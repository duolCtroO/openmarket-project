package oort.cloud.openmarket.review.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCount {
    @Id
    private Long productId;
    private int count;
    private Long ratingSum;

    public static ReviewCount create(Long productId, int count, Long ratingSum){
        ReviewCount reviewCount = new ReviewCount();
        reviewCount.productId = productId;
        reviewCount.count = count;
        reviewCount.ratingSum = ratingSum;
        return reviewCount;
    }

    public void increaseCount(){
        this.count++;
    }

    public void decreaseCount(){
        this.count--;
    }

    public void increaseRatingSum(int rating){
        this.ratingSum += rating;
    }

    public void decreaseRatingSum(int rating){
        this.ratingSum -= rating;
    }
}
