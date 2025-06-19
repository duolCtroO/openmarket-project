package oort.cloud.openmarket.products.repository;

import jakarta.persistence.LockModeType;
import oort.cloud.openmarket.products.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductsRepository extends JpaRepository<Products, Long>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p from Products p where p.productId = :productId
    """)
    Products findByProductIdPessimisticLock(@Param("productId") Long productId);



}
