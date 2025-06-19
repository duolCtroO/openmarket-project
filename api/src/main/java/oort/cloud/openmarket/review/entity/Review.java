package oort.cloud.openmarket.review.entity;

import jakarta.persistence.*;
import lombok.*;
import oort.cloud.openmarket.common.entity.BaseTimeEntity;
import oort.cloud.openmarket.order.entity.OrderItem;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.user.entity.Users;

import java.util.Objects;

@Entity
@Table(name = "review")
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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


