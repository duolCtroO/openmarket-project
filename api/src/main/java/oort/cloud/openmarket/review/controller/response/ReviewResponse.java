package oort.cloud.openmarket.review.controller.response;

import lombok.Getter;
import lombok.ToString;
import oort.cloud.openmarket.review.entity.Review;

@ToString
@Getter
public class ReviewResponse {
    private Long reviewId;

    public static ReviewResponse from(Review review){
        ReviewResponse response = new ReviewResponse();
        response.reviewId = review.getReviewId();
        return response;
    }
}
