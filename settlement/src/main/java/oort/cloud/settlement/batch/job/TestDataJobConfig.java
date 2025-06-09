package oort.cloud.settlement.batch.job;

import lombok.extern.slf4j.Slf4j;
import oort.cloud.settlement.batch.listener.common.ElapsedTimeJobListener;
import oort.cloud.settlement.batch.tasklet.test.ProductTestDataGenerator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *  실행 커맨드 : ./gradlew :settlement:bootRun --args="--spring.batch.job.name=testDataJob run.id=$(date +%s)"
 *  테스트 용도 데이터 삽입 Job
 *
 *   Bulk insert 성능 테스트 (10만건 데이터 삽입, 1000건 데이터 100번 수행)
 *
 *   jdbc bulk insert 결과
 *   testDataJob Elapsed Time : 70988 ms (rewriteBatchedStatements=true 설정 없음)
 *   testDataJob Elapsed Time : 3664 ms (rewriteBatchedStatements=true)
 *
 *   mybatis bulk insert 결과
 *   testDataJob Elapsed Time : 7959 ms
 *
 *
 */

@Configuration
@Slf4j
public class TestDataJobConfig {
    private final JobRepository jobRepository;
    private final ProductTestDataGenerator productTestDataGenerator;
    private final PlatformTransactionManager transactionManager;
    private final ElapsedTimeJobListener listener;

    public TestDataJobConfig(JobRepository jobRepository, ProductTestDataGenerator productTestDataGenerator, PlatformTransactionManager transactionManager, ElapsedTimeJobListener listener) {
        this.jobRepository = jobRepository;
        this.productTestDataGenerator = productTestDataGenerator;
        this.transactionManager = transactionManager;
        this.listener = listener;
    }

    @Bean
    public Step testDataStep(){
        return new StepBuilder("testDataStep", jobRepository)
                .tasklet(productTestDataGenerator, transactionManager)
                .build();
    }

    @Bean
    public Job testDataJob(){
        return new JobBuilder("testDataJob", jobRepository)
                .start(testDataStep())
                .listener(listener)
                .build();
    }
}
