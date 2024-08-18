package com.lxz.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lxz.ucenter.mapper.XcMenuMapper;
import com.lxz.ucenter.mapper.XcUserMapper;
import com.lxz.ucenter.model.dto.AuthParamsDto;
import com.lxz.ucenter.model.dto.XcUserExt;
import com.lxz.ucenter.model.po.XcMenu;
import com.lxz.ucenter.model.po.XcUser;
import com.lxz.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/16 下午9:50
 */
@Service
@Component
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;       // 查询用户信息

    @Autowired
    ApplicationContext applicationContext;       // 获取spring容器

    @Autowired
    XcMenuMapper xcMenuMapper;   // 查询用户权限

    @Override
    // 通过用户名密码登录
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ======将传入的json转为AuthParamsDto对象===========
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(username, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("json转换异常");
        }
        // ==========得到认证类型==============
        String type = authParamsDto.getAuthType();
        // 根据认证类型从spring容器中获取对应的AuthService
        String beanName = type + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        // ==========根据认证类型调用AuthService的execute方法进行认证===========
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        // ===========设置用户权限，并转为UserDetails对象===============
        UserDetails userDetails = getUserPrincipal(xcUserExt);
        return userDetails;
    }

    // 设置用户权限，并转为UserDetails对象
    public UserDetails getUserPrincipal(XcUserExt xcUser) {
        // =========根据用户id查询用户权限=========
        // 拿到整个数据列表，但是只需要权限code
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.size() > 0) {
            xcMenus.forEach(item -> {
                // 将权限code添加到permissions中
                permissions.add(item.getCode());
            });
        }
        String[] authorities = permissions.toArray(new String[0]);
        // ======将密码置空，防止泄露==========
        xcUser.setPassword(null);
        // =======将用户信息转json=========
        String userJson = JSON.toJSONString(xcUser);
        // ======构建UserDetails对象=========
        UserDetails build = User.withUsername(userJson).password("").authorities(authorities).build();
        return build;
    }
}
