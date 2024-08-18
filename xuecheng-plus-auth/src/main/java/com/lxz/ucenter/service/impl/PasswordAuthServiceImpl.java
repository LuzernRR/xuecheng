package com.lxz.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxz.ucenter.feignclient.CheckCodeClient;
import com.lxz.ucenter.mapper.XcUserMapper;
import com.lxz.ucenter.model.dto.AuthParamsDto;
import com.lxz.ucenter.model.dto.XcUserExt;
import com.lxz.ucenter.model.po.XcUser;
import com.lxz.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/17 上午10:52
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String name = authParamsDto.getUsername();
        // ===========校验验证码==========
        // 从authParamsDto中获取验证码
        String checkcode = authParamsDto.getCheckcode();
        // 从authParamsDto中获取验证码key
        String checkcodeKey = authParamsDto.getCheckcodekey();
        // 远程调用验证码服务校验验证码
        Boolean verify = checkCodeClient.verify(checkcodeKey, checkcode);
        if (checkcodeKey == null || checkcode == null) {
            throw new RuntimeException("请输入验证码");
        }
        if (!verify) {
            throw new RuntimeException("验证码错误");
        }

        // ===========账号是否存在==========
        // 根据username账号查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, name));
        // 查询到用户不存在，返回null即可，springsecurity会抛出异常
        if (xcUser == null) {
            throw new RuntimeException("用户不存在");
        }
        // 如果密码正确，会封装成一个UserDetails对象给springsecurity返回，由springsecurity进行密码比对
        String passwordDb = xcUser.getPassword();
        // ===============验证密码==================
        // 从authParamsDto中获取用户输入的密码
        String passwordInput = authParamsDto.getPassword();
        // 对比密码
        boolean matches = passwordEncoder.matches(passwordInput, passwordDb);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        // ===========封装返回结果==========
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
