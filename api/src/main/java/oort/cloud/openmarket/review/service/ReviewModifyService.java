package oort.cloud.openmarket.review.service;

import jakarta.transaction.Transactional;
import oort.cloud.openmarket.common.exception.business.NotAllowedActionException;
import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.order.enums.OrderItemStatus;
import oort.cloud.openmarket.order.service.OrderItemService;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.controller.request.UpdateReviewRequest;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.review.repository.ReviewRepository;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class ReviewModifyService implements ReviewRegister{
    private final ReviewRepository reviewRepository;
    private final ReviewFinder reviewFinder;
    private final UserService userService;
    private final OrderItemService orderItemService;

    public ReviewModifyService(ReviewRepository reviewRepository, ReviewFinder reviewFinder, UserService userService, OrderItemService orderItemService) {
        this.reviewRepository = reviewRepository;
        this.reviewFinder = reviewFinder;
        this.userService = userService;
        this.orderItemService = orderItemService;
    }

    @Transactional
    @Override
    public ReviewResponse register(Long userId, CreateReviewRequest request) {
        existReview(userId, request);

        Users user = userService.findUserEntityById(userId);

        OrderItem orderItem = orderItemService.findOrderItemById(request.getOrderItemId());

        isWritable(orderItem, user);

        Products product = orderItem.getProduct();

        return ReviewResponse.of(
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

    private void existReview(Long userId, CreateReviewRequest request) {
        if(reviewFinder.existReview(userId, request.getOrderItemId())){
            throw new NotAllowedActionException("이미 작성된 리뷰가 존재합니다.");
        }
    }

    private void isWritable(OrderItem orderItem, Users user) {
        if(orderItem.getStatus() != OrderItemStatus.COMPLETED){
            throw new NotAllowedActionException("구매확정된 상품 대상으로 리뷰 작성이 가능합니다.");
        }

        if(!orderItem.getOrder().getUser().equals(user)){
            throw new NotAllowedActionException("상품 구매자만 리뷰 작성이 가능합니다.");
        }
    }

    @Transactional
    @Override
    public ReviewResponse modify(Long userId, Long reviewId, UpdateReviewRequest updateReviewRequest) {
        Review review = reviewFinder.findById(reviewId);

        review.updateInfo(userId, updateReviewRequest);

        return ReviewResponse.of(reviewRepository.save(review));
    }

    @Transactional
    @Override
    public void delete(Long userId, Long reviewId) {
        Review review = reviewFinder.findById(reviewId);

        review.delete(userId);

        reviewRepository.save(review);
    }
}
