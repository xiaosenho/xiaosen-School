package com.xiaosenho.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: 作者
 * @create: 2025-02-22 19:39
 * @Description:
 */
@Component
@Slf4j
public class CheckcodeServiceClientFallbackFactory implements FallbackFactory<CheckcodeServiceClient> {
    // 熔断降级处理
    // FallbackFactory可以拿到远程调用的异常信息，而Fallback不能拿到
    @Override
    public CheckcodeServiceClient create(Throwable throwable) {
        return new CheckcodeServiceClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.error("验证码校验失败,验证码key信息:{},熔断异常:{}", key,throwable.getMessage());
                return false;
            }
        };
    }
}
