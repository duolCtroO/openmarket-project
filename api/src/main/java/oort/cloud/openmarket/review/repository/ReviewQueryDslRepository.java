package oort.cloud.openmarket.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.validation.constraints.NotNull;
import oort.cloud.openmarket.review.entity.QReview;
import oort.cloud.openmarket.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewQueryDslRepository {
    private final JPAQueryFactory queryFactory;

    public ReviewQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Review> findAllByProductId(@NotNull Long productId,
                                           Pageable pageable,
                                           OrderSpecifier<?>[] orderSpecifiers
                                           ){
        QReview review = QReview.review;
        BooleanBuilder condition = getCommonCondition(productId, review);
        return queryFactory
                .selectFrom(review)
                .where(condition)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public Long countByProductId(@NotNull Long productId,
                                           Pageable pageable,
                                           OrderSpecifier<?>[] orderSpecifiers
    ){
        QReview review = QReview.review;
        BooleanBuilder condition = getCommonCondition(productId, review);
        return queryFactory
                .select(review.count())
                .from(review)
                .where(condition)
                .fetchOne();
    }

    private BooleanBuilder getCommonCondition(Long productId, QReview review) {
        return new BooleanBuilder()
                .and(review.product.productId.eq(productId))
                .and(review.isDeleted.isFalse());
    }
}
