package oort.cloud.openmarket.products.service;

import oort.cloud.openmarket.category.entity.Category;
import oort.cloud.openmarket.category.service.CategoryService;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.data.ProductRequestTest;
import oort.cloud.openmarket.products.controller.request.ProductRequest;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.enums.ProductsStatus;
import oort.cloud.openmarket.products.repository.ProductsRepository;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private ProductsRepository productsRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private UserService userService;
    @InjectMocks
    private ProductsService productsService;

    private Products product;
    private Category category;
    private Users user;

    @BeforeEach
    void init(){
        product = Products.create("TestProduct", mock(Users.class), "test product", 1000, 10);
        ReflectionTestUtils.setField(product, "productId", 123L);
        category = mock(Category.class);
        user = mock(Users.class);
    }

    @Test
    @DisplayName("상품 등록에 성공한다")
    void success_create_product(){
        ProductRequest request = new ProductRequestTest("TestProduct",
                "testProduct", 10000, 10, 1L);
        Long userId = 4L;

        when(userService.findUserEntityById(userId))
                .thenReturn(user);
        when(categoryService.findCategoryById(1L))
                .thenReturn(category);
        when(productsRepository.save(any(Products.class)))
                .thenReturn(product);

        Long productId = productsService.createProduct(userId, request).getProductId();

        assertThat(productId).isEqualTo(123L);
        verify(productsRepository).save(any(Products.class));
        verify(categoryService).findCategoryById(any());
    }

    @Test
    @DisplayName("상품 수정에 성공한다.")
    void success_product_update(){
        ProductRequest request = new ProductRequestTest("TestProduct",
                "testProduct", 10000, 12, 1L);

        when(productsRepository.findById(123L)).thenReturn(Optional.of(product));
        when(categoryService.findCategoryById(1L)).thenReturn(category);

        productsService.updateProduct(1L, 123L, request);

        assertThat(product.getProductName()).isEqualTo("TestProduct");
        assertThat(product.getStock()).isEqualTo(12);
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정에 성공한다.")
    void fail_product_update(){
        ProductRequest request = new ProductRequestTest("TestProduct",
                "testProduct", 10000, 12, 1L);
        when(productsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productsService.updateProduct(1L, 999L, request))
                .isInstanceOf(NotFoundResourceException.class);
    }

    @Test
    void deleteProduct_성공() {
        when(productsRepository.findById(123L)).thenReturn(Optional.of(product));

        productsService.deleteProduct(123L);

        assertThat(product.getStatus()).isEqualTo(ProductsStatus.DELETED);
    }




}