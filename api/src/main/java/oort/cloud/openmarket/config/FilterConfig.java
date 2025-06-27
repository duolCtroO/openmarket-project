package oort.cloud.openmarket.config;

import oort.cloud.openmarket.config.filter.AdminAuthorizationFilter;
import oort.cloud.openmarket.config.filter.JwtAuthFilter;
import oort.cloud.openmarket.config.filter.SellerAuthorizationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistrationBean(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(jwtAuthFilter);
        frb.addUrlPatterns(
                "/v1/user/*", "/v1/auth/logout", "/v1/order/*",
                        "/v1/seller/*", "/v1/review");
        frb.setOrder(1);
        return frb;
    }

    @Bean
    public FilterRegistrationBean<AdminAuthorizationFilter> adminAuthorizationFilterRegistrationBean(AdminAuthorizationFilter adminAuthorizationFilter) {
        FilterRegistrationBean<AdminAuthorizationFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(adminAuthorizationFilter);
        frb.addUrlPatterns("/v1/admin/*");
        frb.setOrder(2);
        return frb;
    }

    @Bean
    public FilterRegistrationBean<SellerAuthorizationFilter> sellerAuthorizationFilterRegistrationBean(SellerAuthorizationFilter sellerAuthorizationFilter) {
        FilterRegistrationBean<SellerAuthorizationFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(sellerAuthorizationFilter);
        frb.addUrlPatterns("/v1/seller/*");
        frb.setOrder(3);
        return frb;
    }

}
