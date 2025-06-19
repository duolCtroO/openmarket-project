package oort.cloud.openmarket.review.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewQueryDslRepository {
    private final JPAQueryFactory queryFactory;

    public ReviewQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
}
