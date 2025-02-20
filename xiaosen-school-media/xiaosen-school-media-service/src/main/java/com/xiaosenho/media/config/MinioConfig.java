package com.xiaosenho.media.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: 作者
 * @create: 2025-02-18 15:45
 * @Description: Minio客户端配置
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
//@ConfigurationProperties属性注入需要提供显式get和set方法
@Getter
@Setter
public class MinioConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;

    @Bean
    public MinioClient minioClient() {

        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(accessKey, secretKey)
                        .build();
        return minioClient;
    }

}
