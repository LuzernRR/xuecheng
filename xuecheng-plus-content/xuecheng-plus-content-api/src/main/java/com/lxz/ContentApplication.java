package com.lxz;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

// 内容管理服务启动类
@SpringBootApplication
// 生成swagger文档
@EnableSwagger2Doc
@ComponentScan(basePackages = {"com.lxz.content","com.lxz.messagesdk"})
@MapperScan("com.lxz.messagesdk.mapper")
@EnableFeignClients(basePackages = {"com.lxz.content.feignclient"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
