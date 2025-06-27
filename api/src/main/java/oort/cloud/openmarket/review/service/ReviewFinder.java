package oort.cloud.openmarket.review.service;

import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.common.paging.offset.OffsetPageResponse;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.entity.Review;
import org.springframework.data.domain.Pageable;

public interface ReviewFinder {
    OffsetPageResponse<ReviewResponse> findReviews(Long productId, String sortKey, Pageable pageable);

    Review findByProductId(Long productId);

    boolean existReview(Long userId, Long productId);

    Review findById(Long reviewId);
}
