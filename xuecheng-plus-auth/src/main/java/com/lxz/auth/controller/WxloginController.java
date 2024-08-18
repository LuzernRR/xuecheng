package com.lxz.auth.controller;

import com.lxz.ucenter.model.po.XcUser;
import com.lxz.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/17 下午3:17
 */
@Slf4j
@Controller
public class WxloginController {

    @Autowired
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxlogin(String code, String state) {
        // =========拿到微信返回的授权码=============
        log.debug("微信扫码回调：code:{},state:{}", code, state);
        // =========1. 携带授权码申请access_token和openid=========
        // =========2. 通过access_token和openid获取用户信息=========
        // =========3. 根据用户信息查询数据库，如果没有则注册=========
        XcUser xcUser1 = wxAuthService.wxAuth(code);
        if (xcUser1 == null) {
            // 获取用户失败，重定向到错误页面
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser1.getUsername();
        // =====获取用户成功，重定向到登录页面，进入WxAuthServiceImpl自动登录=======
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";
    }
}
