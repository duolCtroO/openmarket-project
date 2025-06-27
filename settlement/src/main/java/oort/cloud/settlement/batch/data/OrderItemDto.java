package oort.cloud.settlement.batch.data;

import lombok.Getter;
import lombok.Setter;
import oort.cloud.settlement.batch.data.enums.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OrderItemDto {
    private Long orderItemId;
    private Long userId;
    private Integer totalPrice;
    private OrderItemStatus status;
    private BigDecimal commissionRate;
    private LocalDate confirmedAt;
    private LocalDate deliveryAt;

    public void changeStatus(OrderItemStatus status){
        this.status = status;
    }

    public void setConfirmedAt(LocalDate confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    @Override
    public String toString() {
        return "OrderItemDto{" +
                "orderItemId=" + orderItemId +
                ", userId=" + userId +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                ", commissionRate=" + commissionRate +
                ", confirmedAt=" + confirmedAt +
                ", deliveryAt=" + deliveryAt +
                '}';
    }
}
