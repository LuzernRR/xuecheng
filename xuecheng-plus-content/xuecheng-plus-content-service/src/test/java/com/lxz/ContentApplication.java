package com.lxz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 内容管理服务启动类
@SpringBootApplication
// 生成swagger文档
@EnableFeignClients(basePackages = {"com.lxz.content.feignclient"})
public class ContentApplication {
    public static void main(String[] args) {
        // ContentApplication.class：指当前类，即应用程序的入口类。
        // 这告诉Spring Boot使用这个类作为启动配置的基础。
        SpringApplication.run(ContentApplication.class, args);
    }
}
