package com.lxz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages={"com.lxz.*.feignclient"})
@MapperScan("com.lxz.learning.mapper")
@SpringBootApplication
public class LearningApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningApiApplication.class, args);
    }

}
