package oort.cloud.settlement.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oort.cloud.settlement.batch.chunk.processor.SettlementProcessor;
import oort.cloud.settlement.batch.data.OrderItemDto;
import oort.cloud.settlement.batch.data.SettlementDto;
import oort.cloud.settlement.batch.query.settlement.WriteSettlementSqlQuery;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLTimeoutException;
import java.time.LocalDate;
import java.util.Map;

/**
 *  실행 커맨드 : ./gradlew :settlement:bootRun --args="--job.name=jdbcSettlementJob confirmedAt=2025-05-20 status=CONFIRMED run.id=$(date +%s)"
 *
 *  일 단위 구매확정 주문상품에 대한 정산 배치
 *  1. 어제 날짜 기준 구매확정(Confirmed) 상태인 주문상품 조회
 *  2. 해당 주문 상품의 카테고리에 해당하는 수수료를 적용하여 수수료 금액과 실 지급액 계산
 *  3. 정산(Settlement) 테이블에 데이터 저장
 */

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcSettlementJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final PagingQueryProvider selectOrderItemQueryProvider;
    private final SettlementProcessor settlementProcessor;
    private final StepExecutionListener settlementStepListener;
    private final RetryListener createSettlementRetryListener;

    @Bean
    public Job settlementJob(){
        return new JobBuilder("jdbcSettlementJob", jobRepository)
                .start(settlementStep())
                .build();
    }

    @Bean
    public Step settlementStep(){
        return new StepBuilder("jdbcSettlementStep", jobRepository)
                .<OrderItemDto, SettlementDto>chunk(2, transactionManager)
                .reader(settlementOrderItemReader(null, null))
                .processor(settlementProcessor)
                .writer(createSettlementWriter())
                .listener(settlementStepListener)
                .faultTolerant()
                .retry(SQLTimeoutException.class)
                .retryLimit(3)
                .processorNonTransactional() // 이미 처리된 Item에 대해서는 캐시를 적용하여 다시 진행하지 않음
                .listener(createSettlementRetryListener)
                .backOffPolicy(new ExponentialBackOffPolicy(){{
                    setInitialInterval(1000L);
                    setMultiplier(3);
                    setMaxInterval(10000L);
                }})
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<OrderItemDto> settlementOrderItemReader(
                                                        @Value("#{jobParameters[confirmedAt]}") LocalDate confirmedAtParam,
                                                        @Value("#{jobParameters[status]}") String status) {
        Map<String, Object> queryParam = Map.of("confirmedAt", confirmedAtParam, "status", status);
        return new JdbcPagingItemReaderBuilder<OrderItemDto>()
                .name("settlementOrderItemReader")
                .dataSource(dataSource)
                .pageSize(10)
                .queryProvider(selectOrderItemQueryProvider)
                .parameterValues(queryParam)
                .beanRowMapper(OrderItemDto.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<SettlementDto> createSettlementWriter() {
        return new JdbcBatchItemWriterBuilder<SettlementDto>()
                .dataSource(dataSource)
                .sql(WriteSettlementSqlQuery.INSERT.getQuery())
                .beanMapped()
                .build();
    }

}
