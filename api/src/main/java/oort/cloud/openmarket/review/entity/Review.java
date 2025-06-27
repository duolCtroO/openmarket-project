package oort.cloud.openmarket.review.entity;

import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.*;
import lombok.*;
import oort.cloud.openmarket.common.entity.BaseTimeEntity;
import oort.cloud.openmarket.common.exception.business.NotAllowedActionException;
import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.review.controller.request.UpdateReviewRequest;
import oort.cloud.openmarket.user.entity.Users;

import java.util.Objects;

@Entity
@ToString(callSuper = true, exclude = {"user", "product", "orderItem"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_orderitem", columnNames = {"user_id", "order_item_id"})
        })
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Products product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    private int rating;

    private String content;

    private boolean isDeleted;

    public static Review create(Users user, Products product, OrderItem orderItem, int rating, String content){
        Review review = new Review();
        review.user = user;
        review.product = product;
        review.orderItem = orderItem;
        review.rating = rating;
        review.content = content;
        review.isDeleted = false;
        return review;
    }

    public void updateInfo(Long userId, UpdateReviewRequest request){
        Assert.state(!isDeleted(), "삭제된 리뷰는 수정이 불가능 합니다.");

        if(this.user.getUserId().equals(userId)){
            throw new NotAllowedActionException("리뷰 작성자만 수정이 가능합니다.");
        }

        this.rating = request.getRating();
        this.content = request.getContent();
    }

    public void delete(Long userId){
        Assert.state(!isDeleted(), "이미 삭제된 리뷰입니다.");

        if(this.user.getUserId().equals(userId)){
            throw new NotAllowedActionException("리뷰 작성자만 삭제가 가능합니다.");
        }

        this.isDeleted = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(reviewId, review.reviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }
}


