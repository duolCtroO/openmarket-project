package oort.cloud.openmarket.review.service;

import com.querydsl.core.types.OrderSpecifier;
import lombok.Getter;
import oort.cloud.openmarket.review.entity.QReview;

import java.util.Arrays;

@Getter
public enum ReviewSortKey {
    LATEST("latest"
    ) {
        @Override
        public OrderSpecifier<?>[] getOrderSpecifiers() {
            return new OrderSpecifier[]{QReview.review.createdAt.desc(), QReview.review.reviewId.desc()};
        }
    },

    RATING_ASC("rating_desc"
    ) {
        @Override
        public OrderSpecifier<?>[] getOrderSpecifiers() {
            return new OrderSpecifier[]{QReview.review.rating.desc(), QReview.review.createdAt.desc(), QReview.review.reviewId.desc()};
        }
    },

    RATING_DESC("rating_asc"
    ) {
        @Override
        public OrderSpecifier<?>[] getOrderSpecifiers() {
            return new OrderSpecifier[]{QReview.review.rating.asc(), QReview.review.createdAt.desc(), QReview.review.reviewId.desc()};
        }
    },
    ;

    private final String key;

    ReviewSortKey(String key) {
        this.key = key;
    }

    public abstract OrderSpecifier<?>[] getOrderSpecifiers();

    public static ReviewSortKey from(String key) {
        return Arrays.stream(values())
                .filter(e -> e.key.equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid sort key: " + key));
    }
}
