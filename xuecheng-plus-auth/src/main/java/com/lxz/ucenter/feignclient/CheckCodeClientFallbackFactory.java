package com.lxz.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/17 下午2:44
 */
@Slf4j
@Component
public class CheckCodeClientFallbackFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务失败");
                return null;
            }
        };
    }
}
