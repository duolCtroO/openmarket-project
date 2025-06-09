package oort.cloud.settlement.mapper.product;

import oort.cloud.settlement.batch.data.ProductDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    void bulkInsert(@Param("products") List<ProductDto> products);
}
