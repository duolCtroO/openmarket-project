package oort.cloud.settlement.batch.chunk.processor;

import lombok.extern.slf4j.Slf4j;
import oort.cloud.settlement.batch.data.OrderItemDto;
import oort.cloud.settlement.batch.data.SettlementDto;
import oort.cloud.settlement.batch.data.enums.SettlementStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
public class SettlementProcessor implements ItemProcessor<OrderItemDto, SettlementDto> {

    @Override
    public SettlementDto process(OrderItemDto item) throws Exception {
        log.info("SettlementProcessor 시작...");
        log.info("OrderItemDto : {}", item);
        int commissionAmount = item.getCommissionRate()
                                    .multiply(BigDecimal.valueOf(item.getTotalPrice()))
                                    .intValue();
        //빌더로 리펙토링 필요
        return SettlementDto.of(
                item.getUserId(),
                item.getTotalPrice() - commissionAmount,
                commissionAmount,
                SettlementStatus.PENDING,
                item.getOrderItemId(),
                LocalDate.now().plusDays(1),
                null
        );
    }
}
