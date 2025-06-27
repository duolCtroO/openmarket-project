package oort.cloud.openmarket.products.service;

import oort.cloud.openmarket.products.controller.request.ProductRequest;
import oort.cloud.openmarket.products.controller.response.CreateProductResponse;
import org.springframework.transaction.annotation.Transactional;

public interface ProductsRegister {

    @Transactional
    CreateProductResponse createProduct(Long userId, ProductRequest request);

    @Transactional
    void updateProduct(Long userId, Long productId, ProductRequest request);

    @Transactional
    void deleteProduct(Long productId);
}
