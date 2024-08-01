package com.lxz.content.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//MapperScan指定了MyBatis接口（Mapper）的包扫描路径。
// MyBatis会在指定的包中查找所有的Mapper接口并自动注册它们。
@MapperScan("com.lxz.content.mapper")
public class MybatisPlusConfig {
    // 表明这个方法将返回一个Bean，该Bean会被Spring的应用上下文管理。
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
