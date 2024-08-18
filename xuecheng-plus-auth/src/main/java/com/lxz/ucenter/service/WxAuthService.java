package com.lxz.ucenter.service;

import com.lxz.ucenter.model.po.XcUser;

public interface WxAuthService {

    // 微信扫码认证
    public XcUser wxAuth(String code);

}
