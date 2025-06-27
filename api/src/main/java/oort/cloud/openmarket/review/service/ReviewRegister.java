package oort.cloud.openmarket.review.service;

import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.controller.request.UpdateReviewRequest;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;

public interface ReviewRegister {
    ReviewResponse register(Long userId, CreateReviewRequest request);

    ReviewResponse modify(Long userId, Long reviewId, UpdateReviewRequest updateReviewRequest);

    void delete(Long userId, Long reviewId);
}
