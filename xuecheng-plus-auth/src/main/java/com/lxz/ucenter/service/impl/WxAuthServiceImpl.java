package com.lxz.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxz.ucenter.mapper.XcUserMapper;
import com.lxz.ucenter.mapper.XcUserRoleMapper;
import com.lxz.ucenter.model.dto.AuthParamsDto;
import com.lxz.ucenter.model.dto.XcUserExt;
import com.lxz.ucenter.model.po.XcUser;
import com.lxz.ucenter.model.po.XcUserRole;
import com.lxz.ucenter.service.AuthService;
import com.lxz.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/17 上午10:53
 */
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    RestTemplate restTemplate;   // 与第三方服务进行交互需要使用RestTemplate

    @Autowired
    XcUserRoleMapper xcUserRoleMapper;

    @Autowired
    WxAuthServiceImpl currentProxy;

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.secret}")
    private String secret;

    @Override
    // 用户重定向后，会执行execute方法，返回用户信息
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }

    @Override
    public XcUser wxAuth(String code) {
        // ======申请令牌=========
        Map<String, String> acessToken = getAcessToken(code);
        String accessToken = acessToken.get("access_token");
        String openid = acessToken.get("openid");
        // ======携带令牌查询用户信息=========
        // 获取用户信息
        Map<String, String> userInfo = getUserInfo(accessToken, openid);
        // ======将用户信息存入数据库=========
        // 存在非事务方法调用事务方法的问题，需要把WxAuthServiceImpl注入，通过代理的方式调用
        XcUser xcUser = currentProxy.saveWxUser(userInfo);
        return xcUser;
    }

    // 获取令牌的方法
    // 携带授权码申请令牌：https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
    private Map<String, String> getAcessToken(String code) {
        // 微信获取令牌的url
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(url_template, appid, secret, code);
        // 发送请求，获取令牌
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 解析令牌
        String result = exchange.getBody();
        // 将json转为map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    // 携带令牌查询用户信息
    private Map<String, String> getUserInfo(String accessToken, String openid) {
        // 微信获取用户信息的url
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(url_template, accessToken, openid);
        // 发送请求，获取用户信息
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // 解析用户信息
        String result = exchange.getBody();
        // 将json转为map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    // 保存用户信息
    @Transactional
    public XcUser saveWxUser(Map<String, String> userInfoMap) {
        // 根据unionid获取用户信息
        String unionid = userInfoMap.get("unionid");
        String nickname = userInfoMap.get("nickname");
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (xcUser != null) {
            return xcUser;
        }
        // 向数据库xcUser中插入用户信息
        xcUser = new XcUser();
        String id = UUID.randomUUID().toString();
        xcUser.setId(id);  // 主键
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(nickname);
        xcUser.setName(nickname);
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        // 向用户角色关系表中插入数据
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(id);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }
}
