package oort.cloud.openmarket.review.controller.response;

import lombok.Getter;
import lombok.ToString;
import oort.cloud.openmarket.review.entity.Review;

import java.time.LocalDateTime;

@ToString
@Getter
public class ReviewResponse {
    private Long reviewId;
    private String content;
    private int rating;
    private LocalDateTime updatedAt;

    public static ReviewResponse of(Review review){
        ReviewResponse response = new ReviewResponse();
        response.reviewId = review.getReviewId();
        response.content = review.getContent();
        response.rating = review.getRating();
        response.updatedAt = review.getUpdatedAt();
        return response;
    }
}
