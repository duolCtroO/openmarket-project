package oort.cloud.openmarket.review.controller;

import oort.cloud.openmarket.auth.annotations.AccessToken;
import oort.cloud.openmarket.auth.data.AccessTokenPayload;
import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/v1/review")
    public ResponseEntity<ReviewResponse> createReview(
            @AccessToken AccessTokenPayload payload,
            @RequestBody CreateReviewRequest createReviewRequest){
        return ResponseEntity.ok()
                .body(reviewService.create(payload.getUserId(), createReviewRequest));
    }


}
