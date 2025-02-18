package com.xiaosenho;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: 作者
 * @create: 2025-02-13 19:39
 * @Description:
 */
@EnableSwagger2Doc
@SpringBootApplication
@EnableDiscoveryClient
public class ContentApplication {
    public static void main(String[] args) {
        //启动
        SpringApplication.run(ContentApplication.class,args);
    }
}
