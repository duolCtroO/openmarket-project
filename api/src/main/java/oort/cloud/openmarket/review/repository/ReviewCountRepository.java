package oort.cloud.openmarket.review.repository;

import jakarta.persistence.LockModeType;
import oort.cloud.openmarket.review.entity.ReviewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCountRepository extends JpaRepository<ReviewCount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReviewCount> findByProductId(Long productId);

}
