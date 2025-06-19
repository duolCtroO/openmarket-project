package oort.cloud.openmarket.order.service;

import oort.cloud.openmarket.common.exception.business.NotAllowedActionException;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.order.controller.reponse.OrderCancelResponse;
import oort.cloud.openmarket.order.controller.reponse.OrderCreateResponse;
import oort.cloud.openmarket.order.controller.request.OrderCancelRequest;
import oort.cloud.openmarket.order.controller.request.OrderCreateRequest;
import oort.cloud.openmarket.order.controller.request.OrderItemCreateRequest;
import oort.cloud.openmarket.order.entity.Order;
import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.order.enums.OrderStatus;
import oort.cloud.openmarket.order.repository.OrderRepository;
import oort.cloud.openmarket.payment.service.PaymentService;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.service.ProductsService;
import oort.cloud.openmarket.products.service.StockService;
import oort.cloud.openmarket.user.entity.Address;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductsService productsService;
    private final PaymentService paymentService;
    private final StockService stockService;

    public OrderService(OrderRepository orderRepository, UserService userService, ProductsService productsService, PaymentService paymentService, StockService stockService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productsService = productsService;
        this.paymentService = paymentService;
        this.stockService = stockService;
    }

    @Transactional
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest request){
        //userId 유저 조회
        Users user = userService.findUserEntityById(userId);

        //Address 조회
        Long addressId = request.getAddressId();
        Address address = user.findAddress(addressId);

        List<OrderItemCreateRequest> orderItemRequests = request.getOrderItemRequests();
        Map<Long, Products> products = getProductsMapFrom(orderItemRequests);

        //OrderItem 생성
        List<OrderItem> orderItems = orderItemRequests.stream().map(req -> {
            Long productId = req.getProductId();
            return OrderItem.createOrderItem(products.get(productId), req.getQuantity());
        }).toList();

        //Order 생성
        Order order = Order.createOrder(
                user,
                address,
                request.getReceiverName(),
                request.getReceiverPhone(),
                orderItems);

        //주문 생성
        orderRepository.save(order);
        //결제 진행
        paymentService.processPayment(order);

        stockService.decreaseProductStockWithPessimisticLock(orderItemRequests);
        return new OrderCreateResponse(order.getOrderId(), order.getExternalOrderId());
    }

    private Map<Long, Products> getProductsMapFrom(List<OrderItemCreateRequest> orderItemRequests) {
        List<Long> productIds = orderItemRequests.stream()
                                                .map(OrderItemCreateRequest::getProductId)
                                                .toList();
        return productsService.getProductListByIds(productIds)
                .stream().collect(Collectors.toMap(Products::getProductId, Function.identity()));
    }

    @Transactional
    public OrderCancelResponse cancel(Long orderId, OrderCancelRequest request) {
        //주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundResourceException("조회된 주문이 없습니다."));

        if(order.getStatus() == OrderStatus.CANCELLED){
            throw new NotAllowedActionException("이미 취소된 주문 입니다.");
        }

        //주문 상품 취소
        //하나 이상의 상품이 배송이 시작됐다면 주문 취소 불가
        order.getOrderItems().forEach(OrderItem::cancel);

        //결제 취소 요청
        paymentService.cancel(order.getExternalOrderId(), request.getReason());

        //주문 상태 -> 취소
        order.setStatus(OrderStatus.CANCELLED);

        return new OrderCancelResponse(orderId);
    }
}
