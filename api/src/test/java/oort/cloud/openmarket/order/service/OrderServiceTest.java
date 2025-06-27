package oort.cloud.openmarket.order.service;

import oort.cloud.openmarket.common.exception.business.NotAllowedActionException;
import oort.cloud.openmarket.common.exception.business.OutOfStockException;
import oort.cloud.openmarket.data.OrderCreateRequestTest;
import oort.cloud.openmarket.data.OrderItemCreateRequestTest;
import oort.cloud.openmarket.order.controller.reponse.OrderCancelResponse;
import oort.cloud.openmarket.order.controller.request.OrderCancelRequest;
import oort.cloud.openmarket.order.controller.request.OrderItemCreateRequest;
import oort.cloud.openmarket.order.entity.Order;
import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.order.enums.OrderStatus;
import oort.cloud.openmarket.order.repository.OrderRepository;
import oort.cloud.openmarket.payment.service.PaymentService;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.service.ProductsFinder;
import oort.cloud.openmarket.products.service.ProductsModifyService;
import oort.cloud.openmarket.user.entity.Address;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProductsFinder productsFinder;
    @Mock
    private PaymentService paymentService;
    @InjectMocks
    private OrderService orderService;

    // 더미 엔티티
    private Users mockUser;
    private Address mockAddress;
    private Products product1, product2;

    //주문 상품 가격 & 수량
    private final int product1Price = 30000;
    private final int product2Price = 10000;
    private final int product1Stock = 10;
    private final int product2Stock = 30;

    @BeforeEach
    void setUp() {
        mockUser = mock(Users.class);
        mockAddress = mock(Address.class);


        product1 = Products.create("test", mockUser, "test", product1Price, product1Stock); // 실제 생성자 대신 setter 또는 reflection으로 필드 채워도 무관
        ReflectionTestUtils.setField(product1, "productId", 100L);
        product2 = Products.create("test", mockUser, "test", product2Price, product2Stock); // 실제 생성자 대신 setter 또는 reflection으로 필드 채워도 무관
        ReflectionTestUtils.setField(product2, "productId", 200L);
    }

    @Test
    @DisplayName("주문 생성이 성공한다.")
    public void success_create_order(){
        //given
        List<OrderItemCreateRequest> orderItems = List.of(new OrderItemCreateRequestTest(100L, 10),
                new OrderItemCreateRequestTest(200L, 1));
        OrderCreateRequestTest orderReq = new OrderCreateRequestTest(
                orderItems,10L, "test", "12312341234");
        List<Long> ids = orderItems.stream().map(OrderItemCreateRequest::getProductId).toList();

        //when
        when(userService.findUserEntityById(1L)).thenReturn(mockUser);
        when(mockUser.findAddress(10L)).thenReturn(mockAddress);
        when(productsFinder.getProductListByIds(ids)).thenReturn(List.of(product1, product2));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(1L, orderReq);

        //then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(mockUser);
        assertThat(saved.getAddress()).isEqualTo(mockAddress);
        assertThat(saved.getReceiverName()).isEqualTo("test");
        assertThat(saved.getReceiverPhone()).isEqualTo("12312341234");
    }

    @Test
    @DisplayName("상품 재고가 없으면 OutOfStockException이 발생한다.")
    public void lack_stock_order_fail(){
        //given
        List<OrderItemCreateRequest> orderItems = List.of(new OrderItemCreateRequestTest(100L, 100),
                new OrderItemCreateRequestTest(200L, 12));
        OrderCreateRequestTest orderReq = new OrderCreateRequestTest(
                orderItems, 10L, "test", "12312341234");
        List<Long> ids = orderItems.stream().map(OrderItemCreateRequest::getProductId).toList();

        //when
        when(userService.findUserEntityById(1L)).thenReturn(mockUser);
        when(mockUser.findAddress(10L)).thenReturn(mockAddress);
        when(productsFinder.getProductListByIds(ids)).thenReturn(List.of(product1, product2));

        assertThrows(OutOfStockException.class, () -> orderService.createOrder(1L, orderReq));
    }

    @Test
    @DisplayName("생성된 주문의 주문가격은 모든 주문 상품의 가격 합산금액이 된다.")
    public void success_create_order_total_price(){
        //given
        List<OrderItemCreateRequest> orderItems = List.of(new OrderItemCreateRequestTest(100L, 10),
                new OrderItemCreateRequestTest(200L, 1));
        OrderCreateRequestTest orderReq = new OrderCreateRequestTest(
                orderItems, 10L, "test", "12312341234");
        List<Long> ids = orderItems.stream().map(OrderItemCreateRequest::getProductId).toList();

        //when
        when(userService.findUserEntityById(1L)).thenReturn(mockUser);
        when(mockUser.findAddress(10L)).thenReturn(mockAddress);
        when(productsFinder.getProductListByIds(ids)).thenReturn(List.of(product1, product2));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(1L, orderReq);

        //then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        int orderItemTotalPrice = saved.getOrderItems()
                .stream()
                .mapToInt(OrderItem::getTotalPrice).sum();

        assertThat(saved.getTotalAmount()).isEqualTo(orderItemTotalPrice);
    }

    @Test
    @DisplayName("주문이 생성되면 주문상품의 가격은 요청한 주문 상품의 수량 * 가격이 된다.")
    public void success_create_orderItem(){
        //given
        int product1OrderQuantity = 8;
        int product2OrderQuantity = 3;

        int expectOrderItemTotalPrice1 = product1Price * product1OrderQuantity;
        int expectOrderItemTotalPrice2 = product2Price * product2OrderQuantity;

        int expectProduct1Stock = product1Stock - product1OrderQuantity;
        int expectProduct2Stock = product2Stock - product2OrderQuantity;

        List<OrderItemCreateRequest> orderItems = List.of(
                new OrderItemCreateRequestTest(100L, product1OrderQuantity),
                new OrderItemCreateRequestTest(200L, product2OrderQuantity));
        OrderCreateRequestTest orderReq = new OrderCreateRequestTest(
                orderItems,10L, "test", "12312341234");
        List<Long> ids = orderItems.stream().map(OrderItemCreateRequest::getProductId).toList();

        //when
        when(userService.findUserEntityById(1L)).thenReturn(mockUser);
        when(mockUser.findAddress(10L)).thenReturn(mockAddress);
        when(productsFinder.getProductListByIds(ids)).thenReturn(List.of(product1, product2));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(1L, orderReq);

        //then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        List<OrderItem> savedOrderItems = saved.getOrderItems();
        assertThat(savedOrderItems).hasSize(2);
        assertThat(savedOrderItems)
                .extracting(OrderItem::getProduct)
                .containsExactlyInAnyOrder(product1, product2);
        //상품의 가격과 동일한지
        assertThat(savedOrderItems)
                .extracting(OrderItem::getPrice)
                .containsExactlyInAnyOrder(product1.getPrice(), product2.getPrice());
        //price * quantity = total 금액 확인
        assertThat(savedOrderItems)
                .extracting(OrderItem::getTotalPrice)
                .containsExactlyInAnyOrder(expectOrderItemTotalPrice1, expectOrderItemTotalPrice2);
        //재고 감소 확인
        assertThat(savedOrderItems)
                .extracting(orderItem -> orderItem.getProduct().getStock())
                .containsExactlyInAnyOrder(expectProduct1Stock, expectProduct2Stock);
    }

    @Test
    void success_order_cancel(){
        //given
        Long orderId = 1L;
        String externalOrderId = UUID.randomUUID().toString();
        Order mockOrder = mock(Order.class);
        List<OrderItem> mockOrderItems = List.of(mock(OrderItem.class),mock(OrderItem.class));
        OrderCancelRequest request = mock(OrderCancelRequest.class);

        //when
        when(request.getReason()).thenReturn("단순 변심");
        when(mockOrder.getExternalOrderId()).thenReturn(externalOrderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(mockOrder.getOrderItems()).thenReturn(mockOrderItems);

        OrderCancelResponse cancelResponse = orderService.cancel(orderId, request);

        //then
        verify(orderRepository).findById(orderId);
        verify(paymentService).cancel(externalOrderId, "단순 변심");
        verify(mockOrder).setStatus(OrderStatus.CANCELLED);
        mockOrderItems.forEach(orderItem -> verify(orderItem).cancel());

        assertThat(orderId).isEqualTo(cancelResponse.getOrderId());
    }

    @Test
    void fail_order_cancel(){
        //given
        Long orderId = 1L;
        Order mockOrder = mock(Order.class);
        OrderItem mockOrderItem = mock(OrderItem.class);
        OrderCancelRequest request = mock(OrderCancelRequest.class);

        //when
        doThrow(new NotAllowedActionException("배송중인 상품이 있을 경우 주문 취소가 불가능 합니다."))
                .when(mockOrderItem).cancel();
        when(mockOrder.getOrderItems()).thenReturn(List.of(mockOrderItem));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.cancel(orderId, request))
                .isInstanceOf(NotAllowedActionException.class);

        verify(paymentService, never()).cancel(any(), anyString());
        verify(mockOrder, never()).setStatus(OrderStatus.CANCELLED);
    }
}