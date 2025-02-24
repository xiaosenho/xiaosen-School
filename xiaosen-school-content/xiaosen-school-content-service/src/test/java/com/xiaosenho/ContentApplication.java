package com.xiaosenho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages={"com.xiaosenho.content.feignclient"})
public class ContentApplication {
    public static void main(String[] args) {
        //启动
        SpringApplication.run(ContentApplication.class,args);
    }
}
