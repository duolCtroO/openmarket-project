package oort.cloud.settlement.batch.tasklet.test;

import oort.cloud.settlement.batch.data.ProductDto;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class JdbcProductInitializer {
    private final JdbcTemplate jdbcTemplate;
    private final String sql = """
            INSERT INTO products
            (user_id, product_name, description, price, stock, status, created_at)
            VALUES
            (?, ?, ?, ?, ?, ?, NOW())
            """;

    public JdbcProductInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void bulkInsert(List<ProductDto> products){
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProductDto productDto = products.get(i);
                ps.setLong(1, productDto.getUserId());
                ps.setString(2, productDto.getProductName());
                ps.setString(3, productDto.getDescription());
                ps.setInt(4, productDto.getPrice());
                ps.setInt(5, productDto.getStock());
                ps.setString(6, productDto.getStatus());
            }

            @Override
            public int getBatchSize() {
                return products.size();
            }
        });
    }
}
