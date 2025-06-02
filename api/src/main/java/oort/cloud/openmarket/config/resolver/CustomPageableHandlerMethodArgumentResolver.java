package oort.cloud.openmarket.config.resolver;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {
    /**
     * Pageble 커스텀 설정
     * 페이지는 1부터 시작
     * 내부 페이지는 0부터 시작
     * 최대 페이지 개수 100개
     */
    public CustomPageableHandlerMethodArgumentResolver() {
        super();
        super.setOneIndexedParameters(true);
        super.setFallbackPageable(PageRequest.of(0, 10));
        super.setMaxPageSize(100);
    }
}
