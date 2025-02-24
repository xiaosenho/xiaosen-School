package com.xiaosenho.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 作者
 * @create: 2025-02-22 19:39
 * @Description:
 */
@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    // 熔断降级处理
    // FallbackFactory可以拿到远程调用的异常信息，而Fallback不能拿到
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("搜索索引创建失败,索引信息:{},熔断异常:{}", courseIndex,throwable.getMessage());
                return false;
            }
        };
    }
}
