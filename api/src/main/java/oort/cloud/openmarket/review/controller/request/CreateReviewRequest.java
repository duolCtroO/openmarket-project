package oort.cloud.openmarket.review.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class CreateReviewRequest {
    private Long orderItemId;
    private String content;
    private int rating;
}
