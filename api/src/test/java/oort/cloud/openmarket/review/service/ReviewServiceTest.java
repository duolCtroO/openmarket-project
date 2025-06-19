package oort.cloud.openmarket.review.service;

import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.order.service.OrderItemService;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.service.ProductsService;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.review.repository.ReviewRepository;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.enums.UserRole;
import oort.cloud.openmarket.user.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    UserService userService;
    @Mock
    ProductsService productsService;
    @Mock
    OrderItemService orderItemService;
    @Mock
    ReviewRepository reviewRepository;
    @InjectMocks
    ReviewService reviewService;


    @DisplayName("리뷰가 정상적으로 등록된다")
    @Test
    void success_create_review(){
        Long orderItemId = 40L;
        Long userId = 4L;
        int rating = 4;
        CreateReviewRequestTest request = new CreateReviewRequestTest(orderItemId, "test", rating);
        Users user = Users.createUser("test@email.com", "test", "test", "12312341234", UserRole.BUYER);
        Products product = Products.create("test", user, "test", 10000, 10000);
        OrderItem orderItem = OrderItem.createOrderItem(product, 10);
        Review savedReview = Review.create(user, product, orderItem, rating, "test");

        when(reviewRepository.existsByUserIdAndOrderItemId(userId, orderItemId)).thenReturn(Boolean.FALSE);
        when(userService.findUserEntityById(userId)).thenReturn(user);
        when(orderItemService.findOrderItemById(orderItemId)).thenReturn(orderItem);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.create(userId, request);

        Assertions.assertEquals(savedReview.getReviewId(), response.getReviewId());
    }


    static class CreateReviewRequestTest extends CreateReviewRequest{
        private Long orderItemId;
        private String content;
        private int rating;

        public CreateReviewRequestTest(Long orderItemId, String content, int rating) {
            this.orderItemId = orderItemId;
            this.content = content;
            this.rating = rating;
        }

        @Override
        public Long getOrderItemId() {
            return orderItemId;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public int getRating() {
            return rating;
        }
    }
}