package com.lxz.auth.config;

import com.lxz.ucenter.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @description:重写DaoAuthenticationProvider的校验密码的方法，
 * 有些方式不需要密码校验，比如短信验证码登录，微信登录
 * @author: 12860
 * @time: 2024/8/17 上午10:27
 */
@Component
public class DAOAuthenticationProviderCustom extends DaoAuthenticationProvider {
    @Autowired
    // 重写setUserDetailsService方法，将自定义的UserDetailsService注入
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }
    @Override
    // 重写additionalAuthenticationChecks方法，不进行密码校验
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }
}
