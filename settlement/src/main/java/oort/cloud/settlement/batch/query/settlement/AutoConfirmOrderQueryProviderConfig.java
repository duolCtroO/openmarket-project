package oort.cloud.settlement.batch.query.settlement;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class AutoConfirmOrderQueryProviderConfig {

    @Bean
    public PagingQueryProvider selectDeliveredOrderItemQueryProvider(DataSource dataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        queryProviderFactoryBean.setDataSource(dataSource);
        queryProviderFactoryBean.setSelectClause("""
                SELECT  order_item_id,
                        status,
                        delivered_at
                """);
        queryProviderFactoryBean.setFromClause(
                """
                FROM order_item
                """);
        queryProviderFactoryBean.setWhereClause("""
                WHERE status = :status
                AND delivered_at <= :targetDate
                """);
        queryProviderFactoryBean.setSortKeys(Map.of("order_item_id", Order.ASCENDING));
        return queryProviderFactoryBean.getObject();
    }
}
