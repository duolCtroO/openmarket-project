package oort.cloud.settlement.batch.data;

import lombok.Getter;
import oort.cloud.settlement.batch.data.enums.SettlementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class SettlementDto {
    private Long orderItemId;
    private Long userId;
    private int settlementAmount;
    private int commissionAmount;
    private LocalDate scheduledAt;
    private LocalDate paidAt;
    private SettlementStatus status;
    private LocalDateTime createdAt;
    protected SettlementDto(){}

    public static SettlementDto of(Long userId, int settlementAmount, int commissionAmount, SettlementStatus status,
                                Long orderItemId, LocalDate scheduledAt, LocalDate paidAt){
        SettlementDto settlement = new SettlementDto();
        settlement.userId = userId;
        settlement.orderItemId = orderItemId;
        settlement.settlementAmount = settlementAmount;
        settlement.commissionAmount = commissionAmount;
        settlement.scheduledAt = scheduledAt;
        settlement.paidAt = paidAt;
        settlement.status = status;
        settlement.createdAt = LocalDateTime.now();
        return settlement;
    }
}
