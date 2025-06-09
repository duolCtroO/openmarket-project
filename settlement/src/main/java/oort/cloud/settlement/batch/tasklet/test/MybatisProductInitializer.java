package oort.cloud.settlement.batch.tasklet.test;

import oort.cloud.settlement.batch.data.ProductDto;
import oort.cloud.settlement.mapper.product.ProductMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MybatisProductInitializer {
    private final ProductMapper mapper;

    public MybatisProductInitializer(ProductMapper mapper) {
        this.mapper = mapper;
    }

    public void bulkInsert(List<ProductDto> products){
        mapper.bulkInsert(products);
    }
}
