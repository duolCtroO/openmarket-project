package oort.cloud.settlement.batch.tasklet.test;

import lombok.extern.slf4j.Slf4j;
import oort.cloud.openmarket.enums.ProductsStatus;
import oort.cloud.settlement.batch.data.ProductDto;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class ProductTestDataGenerator implements Tasklet {
    private final JdbcProductInitializer jdbcProductInitializer;
    private final MybatisProductInitializer mybatisProductInitializer;
    private static final int BULK_SIZE = 1000;
    private static final int REPEAT_SIZE = 3000;
    private int processCount = 1;

    public ProductTestDataGenerator(JdbcProductInitializer jdbcProductInitializer, MybatisProductInitializer mybatisProductInitializer) {
        this.jdbcProductInitializer = jdbcProductInitializer;
        this.mybatisProductInitializer = mybatisProductInitializer;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<ProductDto> temp = new ArrayList<>();
        Random ran = new Random();
        for(int i = 0; i < BULK_SIZE; i++){
            ProductDto product = new ProductDto(
                    4L,
                    "Test Product" + i,
                    "This is test product",
                    ran.nextInt(10000, 5000000),
                    ran.nextInt(10, 10000000),
                    ProductsStatus.values()[ran.nextInt(ProductsStatus.values().length)].name()
            );
            temp.add(product);
        }

        jdbcProductInitializer.bulkInsert(temp);
//        mybatisProductInitializer.bulkInsert(temp);

        if(processCount < REPEAT_SIZE){
            processCount++;
            return RepeatStatus.CONTINUABLE;
        }
        return RepeatStatus.FINISHED;
    }
}
