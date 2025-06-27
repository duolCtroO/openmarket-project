package oort.cloud.settlement.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oort.cloud.settlement.batch.data.OrderItemDto;
import oort.cloud.settlement.batch.data.enums.OrderItemStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.SQLTimeoutException;
import java.time.LocalDate;
import java.util.Map;

/**
 *    배달완료인 주문상품 중 배달완료 날짜가 7일 지난 상품에 대해 자동으로 구매확정 상태 변경
 *    1. 배달완료 날짜가 7일이 지난 주문상품 조회
 *    2. 주문상품의 상태를 구매확정으로 변경
 *
 *    ./gradlew :settlement:bootRun --args="--spring.batch.job.name=autoConfirmOrderJob run.id=$(date +%s)"
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AutoConfirmOrderJobConfig {
    public final JobRepository jobRepository;
    public final DataSource dataSource;
    public final PlatformTransactionManager transactionManager;
    private final PagingQueryProvider selectDeliveredOrderItemQueryProvider;

    @Bean
    public Job autoConfirmOrderJob(){
        return new JobBuilder("autoConfirmOrderJob", jobRepository)
                .start(autoConfirmOrderStep())
                .build()
                ;
    }

    @Bean
    public Step autoConfirmOrderStep(){
        return new StepBuilder("autoConfirmOrderStep", jobRepository)
                .<OrderItemDto, OrderItemDto>chunk(2, transactionManager)
                .reader(deliveredOrderItemReader())
                .processor(autoConfirmOrderProcessor())
                .writer(orderItemStatusWriter())
                .faultTolerant()
                .retry(SQLTimeoutException.class)
                .retryLimit(3)
                .processorNonTransactional() // 이미 처리된 Item에 대해서는 캐시를 적용하여 다시 진행하지 않음
                .backOffPolicy(new ExponentialBackOffPolicy(){{
                    setInitialInterval(1000L);
                    setMultiplier(3);
                    setMaxInterval(10000L);
                }})
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<OrderItemDto> deliveredOrderItemReader() {
        Map<String, Object> queryParam = Map.of("status", OrderItemStatus.DELIVERED.name(),
                "targetDate", LocalDate.now().minusDays(7));
        return new JdbcPagingItemReaderBuilder<OrderItemDto>()
                .name("autoConfirmOrderItemReader")
                .dataSource(dataSource)
                .pageSize(10)
                .queryProvider(selectDeliveredOrderItemQueryProvider)
                .parameterValues(queryParam)
                .beanRowMapper(OrderItemDto.class)
                .build();
    }

    @Bean
    public ItemProcessor<OrderItemDto, OrderItemDto> autoConfirmOrderProcessor() {
        return orderItem -> {
            log.info("AutoConfirmOrderProcessor : {}", orderItem);
            orderItem.changeStatus(OrderItemStatus.CONFIRMED);
            orderItem.setConfirmedAt(LocalDate.now());
            return orderItem;
        };
    }

    @Bean
    public JdbcBatchItemWriter<OrderItemDto> orderItemStatusWriter() {
        return new JdbcBatchItemWriterBuilder<OrderItemDto>()
                .dataSource(dataSource)
                .sql("""
                        UPDATE order_item
                        SET status = ?,
                        confirmed_at = ?
                        WHERE order_item_id = ?
                        """)
                .itemPreparedStatementSetter((ps, orderItem) -> {
                    orderItem.setString(1, ps.getStatus().name());
                    orderItem.setDate(2, Date.valueOf(ps.getConfirmedAt()));
                    orderItem.setLong(3, ps.getOrderItemId());
                })
                .assertUpdates(true)
                .build();
    }

}
