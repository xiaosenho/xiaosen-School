package com.xiaosenho.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 作者
 * @create: 2025-02-22 16:03
 * @Description:
 */
@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    // 熔断降级处理
    // FallbackFactory可以拿到远程调用的异常信息，而Fallback不能拿到
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                log.debug("媒资上传远程服务调用失败:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
