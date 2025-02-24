package com.xiaosenho.ucenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

// value微服务名
@FeignClient(value = "checkcode", fallbackFactory = CheckcodeServiceClientFallbackFactory.class)
public interface CheckcodeServiceClient {
    @PostMapping("/checkcode/verify")
    public Boolean verify(@RequestParam("key") String key,@RequestParam("code") String code);
}
