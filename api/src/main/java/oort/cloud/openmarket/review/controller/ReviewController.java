package oort.cloud.openmarket.review.controller;

import oort.cloud.openmarket.auth.annotations.AccessToken;
import oort.cloud.openmarket.auth.data.AccessTokenPayload;
import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.review.controller.request.CreateReviewRequest;
import oort.cloud.openmarket.review.controller.request.UpdateReviewRequest;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.service.ReviewFinder;
import oort.cloud.openmarket.review.service.ReviewRegister;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ReviewController {

    private final ReviewRegister reviewRegister;
    private final ReviewFinder reviewFinder;

    public ReviewController(ReviewRegister reviewRegister, ReviewFinder reviewFinder) {
        this.reviewRegister = reviewRegister;
        this.reviewFinder = reviewFinder;
    }


    @PostMapping("/v1/review")
    public ResponseEntity<ReviewResponse> createReview(
            @AccessToken AccessTokenPayload payload,
            @RequestBody CreateReviewRequest createReviewRequest){
        return ResponseEntity.ok()
                .body(reviewRegister.register(payload.getUserId(), createReviewRequest));
    }

    @PutMapping("/v1/review/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @AccessToken AccessTokenPayload payload,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody UpdateReviewRequest updateReviewRequest
    ){

        return ResponseEntity.ok()
                .body(reviewRegister.modify(payload.getUserId(), reviewId, updateReviewRequest))
                ;
    }

    @DeleteMapping("/v1/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AccessToken AccessTokenPayload payload,
            @PathVariable("reviewId") Long reviewId
    ){
        reviewRegister.delete(payload.getUserId(), reviewId);
        return ResponseEntity.ok().build();
    }


}
