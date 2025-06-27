package oort.cloud.openmarket.review.repository;

import oort.cloud.openmarket.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUser_UserIdAndOrderItem_OrderItemId(Long userId, Long orderItemId);

    Optional<Review> findByProduct_ProductId(Long productId);

}
