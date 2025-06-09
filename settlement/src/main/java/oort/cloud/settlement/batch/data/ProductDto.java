package oort.cloud.settlement.batch.data;

import java.time.LocalDateTime;

public class ProductDto {
    private Long userId;
    private String productName;
    private String description;
    private int price;
    private int stock;
    private String status;
    private LocalDateTime salesAt;

    public ProductDto(Long userId, String productName, String description, int price, int stock, String status) {
        this.userId = userId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

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

    public String getStatus() {
        return status;
    }

    public LocalDateTime getSalesAt() {
        return salesAt;
    }



    @Override
    public String toString() {
        return "ProductDto{" +
                "userId=" + userId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", status='" + status + '\'' +
                ", salesAt=" + salesAt +
                '}';
    }
}
