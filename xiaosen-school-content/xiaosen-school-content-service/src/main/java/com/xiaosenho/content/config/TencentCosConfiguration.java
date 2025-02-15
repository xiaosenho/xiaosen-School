package com.xiaosenho.content.config;

import com.xiaosenho.content.properties.TencentCosProperties;
import com.xiaosenho.content.utils.TencentCosUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: 作者
 * @create: 2024-12-11 17:02
 * @Description:
 */
@Configuration
@Slf4j
public class TencentCosConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public TencentCosUtil tencentCosUtil(TencentCosProperties tencentCosProperties) {
        log.info("腾讯云存储服务工具初始化……");
        return new TencentCosUtil(tencentCosProperties.getAccessKey(),tencentCosProperties.getSecretKey(),
                tencentCosProperties.getRegion(),tencentCosProperties.getBucket(),tencentCosProperties.getFilePath(),tencentCosProperties.getBaseUrl());
    }
}
