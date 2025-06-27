package oort.cloud.openmarket.products.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import oort.cloud.openmarket.category.entity.Category;
import oort.cloud.openmarket.category.service.CategoryService;
import oort.cloud.openmarket.common.exception.auth.UnauthorizedAccessException;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.common.paging.cusor.Cursor;
import oort.cloud.openmarket.common.paging.cusor.CursorPageRequest;
import oort.cloud.openmarket.common.paging.cusor.CursorPageResponse;
import oort.cloud.openmarket.common.paging.cusor.CursorUtil;
import oort.cloud.openmarket.products.controller.request.ProductRequest;
import oort.cloud.openmarket.products.controller.response.CreateProductResponse;
import oort.cloud.openmarket.products.controller.response.ProductDetailResponse;
import oort.cloud.openmarket.products.controller.response.ProductsResponse;
import oort.cloud.openmarket.products.entity.ProductCategory;
import oort.cloud.openmarket.products.entity.Products;
import oort.cloud.openmarket.products.enums.ProductCursorStrategy;
import oort.cloud.openmarket.products.enums.ProductsCursorField;
import oort.cloud.openmarket.products.enums.ProductsStatus;
import oort.cloud.openmarket.products.repository.ProductQueryDslRepository;
import oort.cloud.openmarket.products.repository.ProductsRepository;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductsModifyService implements ProductsRegister {
    private final ProductsRepository productsRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    public ProductsModifyService(ProductsRepository productsRepository, CategoryService categoryService, UserService userService) {
        this.productsRepository = productsRepository;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @Transactional
    public CreateProductResponse createProduct(Long userId, ProductRequest request) {
        Users user = userService.findUserEntityById(userId);
        Products product = Products.create(
                request.getProductName(),
                user,
                request.getDescription(),
                request.getPrice(),
                request.getStock()
        );

        Category category = categoryService.findCategoryById(request.getCategoryId());
        ProductCategory productCategory = ProductCategory.of(product, category, LocalDateTime.now());
        product.addProductCategories(productCategory);

        return new CreateProductResponse(productsRepository.save(product).getProductId());
    }

    @Transactional
    public void updateProduct(Long userId, Long productId, ProductRequest request){
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundResourceException("조회된 상품이 없습니다."));

        if(userId.equals(product.getUser().getUserId()))
            throw new UnauthorizedAccessException();

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.getProductCategories().clear();

        Category category = categoryService.findCategoryById(request.getCategoryId());
        ProductCategory productCategory = ProductCategory.of(product, category, LocalDateTime.now());
        product.getProductCategories().add(productCategory);
    }

    @Transactional
    public void deleteProduct(Long productId){
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundResourceException("조회된 상품이 없습니다."));
        product.setStatus(ProductsStatus.DELETED);
    }
}
