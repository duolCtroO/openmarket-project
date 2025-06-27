package oort.cloud.openmarket.products.controller;

import jakarta.validation.Valid;
import oort.cloud.openmarket.auth.annotations.AccessToken;
import oort.cloud.openmarket.auth.data.AccessTokenPayload;
import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.common.paging.cusor.CursorPageRequest;
import oort.cloud.openmarket.common.paging.offset.OffsetPageResponse;
import oort.cloud.openmarket.products.controller.response.CreateProductResponse;
import oort.cloud.openmarket.products.controller.response.ProductDetailResponse;
import oort.cloud.openmarket.products.controller.request.ProductRequest;
import oort.cloud.openmarket.products.controller.response.ProductsResponse;
import oort.cloud.openmarket.products.service.ProductsFinder;
import oort.cloud.openmarket.products.service.ProductsModifyService;
import oort.cloud.openmarket.products.service.ProductsRegister;
import oort.cloud.openmarket.review.controller.response.ReviewResponse;
import oort.cloud.openmarket.review.service.ReviewFinder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    private final ProductsRegister productsRegister;
    private final ProductsFinder productsFinder;
    private final ReviewFinder reviewFinder;

    public ProductController(ProductsModifyService productsRegister, ProductsFinder productsFinder, ReviewFinder reviewFinder) {
        this.productsRegister = productsRegister;
        this.productsFinder = productsFinder;
        this.reviewFinder = reviewFinder;
    }

    @PostMapping("/v1/seller/products")
    public ResponseEntity<CreateProductResponse> createProduct(@RequestBody @Valid ProductRequest request,
                                                               @AccessToken AccessTokenPayload payload){
        return ResponseEntity.ok()
                .body(productsRegister.createProduct(payload.getUserId(), request));
    }

    @PutMapping("/v1/seller/products/{productId}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long productId,
                                              @RequestBody @Valid ProductRequest request,
                                              @AccessToken AccessTokenPayload payload){
        productsRegister.updateProduct(payload.getUserId(), productId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/v1/admin/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId){
        productsRegister.deleteProduct(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/products/categories/{categoryId}")
    public ResponseEntity<CursorPageResponse<ProductsResponse>> getCategoryProductList(
            @ModelAttribute @Valid CursorPageRequest request,
            @PathVariable Long categoryId
    ){
        return ResponseEntity.ok()
                .body(productsFinder.find(categoryId, request));
    }

    @GetMapping("/v1/products/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long productId){
        return ResponseEntity.ok()
                .body(productsFinder.getProductDetail(productId));
    }

    @GetMapping("/v1/products/{productId}/reviews")
    public ResponseEntity<OffsetPageResponse<ReviewResponse>> getReviewList(
            @PathVariable("productId") Long productId,
            @RequestParam("sortKey") String sortKey,
            Pageable pageable
    ){
        return ResponseEntity.ok()
                .body(reviewFinder.findReviews(productId, sortKey, pageable));
    }
}
