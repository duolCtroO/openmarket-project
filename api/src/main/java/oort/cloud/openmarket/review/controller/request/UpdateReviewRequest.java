package oort.cloud.openmarket.review.controller.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UpdateReviewRequest {
    private String content;
    private int rating;
}
