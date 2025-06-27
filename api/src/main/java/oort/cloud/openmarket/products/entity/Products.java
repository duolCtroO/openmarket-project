package oort.cloud.openmarket.products.entity;

import jakarta.persistence.*;
import oort.cloud.openmarket.common.entity.BaseTimeEntity;
import oort.cloud.openmarket.common.exception.business.OutOfStockException;
import oort.cloud.openmarket.products.enums.ProductsStatus;
import oort.cloud.openmarket.review.entity.Review;
import oort.cloud.openmarket.user.entity.Users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Products extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

//    @Version
//    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "product_name")
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private int price;

    @Column(name = "stock")
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductsStatus status;

    @Column(name = "sales_at")
    private LocalDateTime salesAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    protected Products(){}

    public static Products create(String productName, Users user, String description, int price, int stock){
        Products products = new Products();
        products.productName = productName;
        products.user = user;
        products.description = description;
        products.price = price;
        products.stock = stock;
        products.status = ProductsStatus.READY;
        return products;
    }

    public void addStock(int count){
        this.stock += count;
    }

    public void removeStock(int quantity) {
        if (stock < quantity){
            throw new OutOfStockException();
        }
        this.stock -= quantity;
    }

    public void addProductCategories(ProductCategory productCategory){
        productCategories.add(productCategory);
        productCategory.setProduct(this);
    }

    public List<ProductCategory> getProductCategories() {
        return productCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Products products = (Products) o;
        return Objects.equals(productId, products.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setStatus(ProductsStatus status) {
        if(ProductsStatus.SALE == status){
            this.salesAt = LocalDateTime.now();
        }
        this.status = status;
    }

    public void setSalesAt(LocalDateTime salesAt) {
        this.salesAt = salesAt;
    }

    public Long getProductId() {
        return productId;
    }

    public Users getUser(){ return user; }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public ProductsStatus getStatus() {
        return status;
    }

    public LocalDateTime getSalesAt(){
        return salesAt;
    }

    @Override
    public String toString() {
        return "Products{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", status=" + status +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
