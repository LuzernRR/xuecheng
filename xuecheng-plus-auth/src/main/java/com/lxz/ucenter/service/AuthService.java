package com.lxz.ucenter.service;


import com.lxz.ucenter.model.dto.AuthParamsDto;
import com.lxz.ucenter.model.dto.XcUserExt;

// 统一的认证接口
public interface AuthService {
    // 执行认证
    public XcUserExt execute(AuthParamsDto authParamsDto);
}
