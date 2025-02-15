package com.xiaosenho.content.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: 作者
 * @create: 2024-12-11 17:00
 * @Description:
 */
@Component
@ConfigurationProperties(prefix = "sky.cos")
@Data
public class TencentCosProperties {
    private String accessKey;

    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件域名
     */
    private String baseUrl;
}
