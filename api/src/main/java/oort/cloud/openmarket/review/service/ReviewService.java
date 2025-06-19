package oort.cloud.openmarket.review.service;

import oort.cloud.openmarket.common.exception.business.NotAllowedActionException;
import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.order.service.OrderItemService;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.service.ProductsService;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.review.repository.ReviewRepository;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final OrderItemService orderItemService;

    public ReviewService(ReviewRepository reviewRepository, UserService userService, OrderItemService orderItemService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.orderItemService = orderItemService;
    }

    @Transactional
    public ReviewResponse create(Long userId, CreateReviewRequest request){
        if(reviewRepository.existsByUserIdAndOrderItemId(userId, request.getOrderItemId())){
            throw new NotAllowedActionException("이미 등록된 리뷰가 있습니다.");
        }
        Users user = userService.findUserEntityById(userId);
        OrderItem orderItem = orderItemService.findOrderItemById(request.getOrderItemId());
        Products product = orderItem.getProduct();
        return ReviewResponse.from(
                reviewRepository.save(
                        Review.create(
                                user,
                                product,
                                orderItem,
                                request.getRating(),
                                request.getContent()
                        )
                )
        );
    }
}
