package oort.cloud.openmarket.products.service;

import oort.cloud.openmarket.order.controller.request.OrderCreateRequest;
import oort.cloud.openmarket.order.controller.request.OrderItemCreateRequest;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.repository.ProductsRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {
    private final ProductsRepository productsRepository;

    public StockService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @Async(value = "stockExecutor")
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void decreaseProductStockWithPessimisticLock(List<OrderItemCreateRequest> requests){
        requests.forEach(req -> {
            Products product = productsRepository.findByProductIdPessimisticLock(req.getProductId());
            product.removeStock(req.getQuantity());
        });
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 25,
            backoff = @Backoff(delay = 200)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseProductStockWithOptimisticLock(Long productId, int quantity){
        Products product = productsRepository.findById(productId).get();
        product.removeStock(quantity);
    }
}
