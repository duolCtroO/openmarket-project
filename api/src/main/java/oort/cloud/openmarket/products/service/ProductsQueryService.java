package oort.cloud.openmarket.products.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.common.paging.cusor.Cursor;
import oort.cloud.openmarket.common.paging.cusor.CursorPageRequest;
import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.common.paging.cusor.CursorUtil;
import oort.cloud.openmarket.products.controller.response.ProductDetailResponse;
import oort.cloud.openmarket.products.controller.response.ProductsResponse;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.enums.ProductCursorStrategy;
import oort.cloud.openmarket.products.enums.ProductsCursorField;
import oort.cloud.openmarket.products.repository.ProductQueryDslRepository;
import oort.cloud.openmarket.products.repository.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsQueryService implements ProductsFinder{
    private final ProductsRepository productsRepository;
    private final CursorUtil cursorUtil;
    private final ProductQueryDslRepository productQueryDslRepository;

    public ProductsQueryService(ProductsRepository productsRepository, CursorUtil cursorUtil, ProductQueryDslRepository productQueryDslRepository) {
        this.productsRepository = productsRepository;
        this.cursorUtil = cursorUtil;
        this.productQueryDslRepository = productQueryDslRepository;
    }

    @Override
    public CursorPageResponse<ProductsResponse> find(Long categoryId, CursorPageRequest request) {
        String sortKeyString = request.getSortKey();
        String cursorData = request.getCursor();
        int size = request.getSize();

        ProductCursorStrategy sortKey = ProductCursorStrategy.getSearchSortKey(sortKeyString);
        List<OrderSpecifier<?>> orderSpecifiers = sortKey.getOrderSpecifiers();

        // 커서 조건 데이터 확인 없으면 null 리턴
        Cursor<ProductsCursorField> cursor = cursorUtil.decodeCursor(cursorData, ProductsCursorField.class);

        Optional<BooleanExpression> cursorCondition = sortKey.getBooleanExpression(cursor);

        List<ProductsResponse> contents = productQueryDslRepository.findByCategoryWithSortAndCursor(
                categoryId,
                cursorCondition.orElse(null),
                orderSpecifiers,
                size
        );
        // 다음 커서 생성
        Optional<String> nextCursor = getNextCursor(sortKey, contents);

        return new CursorPageResponse<>(contents, nextCursor.orElse(null));
    }

    private Optional<String> getNextCursor(ProductCursorStrategy sortKey, List<ProductsResponse> contents) {
        if(contents.isEmpty()){
            return Optional.empty();
        }

        ProductsResponse last = contents.get(contents.size() - 1);
        Cursor<ProductsCursorField> next = new Cursor<>();

        sortKey.getCursorFields().forEach(field -> {
            next.put(field, field.extract(last));
        });

        return Optional.of(cursorUtil.createCursor(next));
    }

    @Override
    public ProductDetailResponse getProductDetail(Long productId) {
        return productsRepository.findById(productId)
                .map(ProductDetailResponse::new)
                .orElseThrow(() -> new NotFoundResourceException("조회된 상품이 없습니다."));
    }

    @Override
    public List<Products> getProductListByIds(List<Long> productIds) {
        List<Products> findProducts = productsRepository.findAllById(productIds);
        if(findProducts.size() != productIds.size())
            throw new NotFoundResourceException("조회된 상품이 없습니다.");
        return findProducts;
    }
}
