package oort.cloud.openmarket.review.service;

import com.querydsl.core.types.OrderSpecifier;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.common.paging.offset.OffsetPageResponse;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.review.repository.ReviewQueryDslRepository;
import oort.cloud.openmarket.review.repository.ReviewRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ReviewQueryService implements ReviewFinder{
    private final ReviewRepository reviewRepository;
    private final ReviewQueryDslRepository reviewQueryDslRepository;

    public ReviewQueryService(ReviewRepository reviewRepository, ReviewQueryDslRepository reviewQueryDslRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewQueryDslRepository = reviewQueryDslRepository;
    }

    @Override
    public OffsetPageResponse<ReviewResponse> findReviews(Long productId, String sortKey, Pageable pageable) {
        ReviewSortKey reviewCursor = ReviewSortKey.from(sortKey);

        OrderSpecifier<?>[] orderSpecifiers = reviewCursor.getOrderSpecifiers();

        List<Review> foundReviews = reviewQueryDslRepository.findAllByProductId(productId, pageable, orderSpecifiers);

        Long totalCount = reviewQueryDslRepository.countByProductId(productId, pageable, orderSpecifiers);

        List<ReviewResponse> responses = foundReviews.stream().map(ReviewResponse::of).toList();

        return new OffsetPageResponse<>(responses,
                pageable.getPageNumber(), pageable.getPageSize(), Objects.requireNonNullElse(totalCount, 0L));
    }

    @Override
    public Review findByProductId(Long productId) {
        return reviewRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new NotFoundResourceException("저장된 리뷰가 없습니다."));
    }

    @Override
    public boolean existReview(Long userId, Long orderItemId) {
        return reviewRepository.existsByUser_UserIdAndOrderItem_OrderItemId(userId, orderItemId);
    }

    @Override
    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundResourceException("저장된 리뷰가 없습니다. Review ID : " + reviewId));
    }
}
