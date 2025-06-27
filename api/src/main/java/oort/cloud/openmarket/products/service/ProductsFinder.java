package oort.cloud.openmarket.products.service;

import oort.cloud.openmarket.common.paging.cusor.CursorPageRequest;
import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.products.controller.response.ProductDetailResponse;
import oort.cloud.openmarket.products.controller.response.ProductsResponse;
import oort.cloud.openmarket.products.entity.Products;

import java.util.List;

public interface ProductsFinder {

    CursorPageResponse<ProductsResponse> find(Long categoryId, CursorPageRequest request);

    ProductDetailResponse getProductDetail(Long productId);

    List<Products> getProductListByIds(List<Long> productIds);
}
