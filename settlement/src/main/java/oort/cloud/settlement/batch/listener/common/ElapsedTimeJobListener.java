package oort.cloud.settlement.batch.listener.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ElapsedTimeJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
        String jobName = jobExecution.getJobInstance().getJobName();
        log.info("{} Start...", jobName);;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long startTime = jobExecution.getExecutionContext().getLong("startTime");
        long endTime = System.currentTimeMillis();
        String jobName = jobExecution.getJobInstance().getJobName();
        log.info("{} Elapsed Time : {} ms", jobName, endTime - startTime);
        log.info("{} End...", jobName);
    }
}
