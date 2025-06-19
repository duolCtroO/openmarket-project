package oort.cloud.openmarket.review.repository;

import oort.cloud.openmarket.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndOrderItemId(Long userId, Long orderItemId);
}
