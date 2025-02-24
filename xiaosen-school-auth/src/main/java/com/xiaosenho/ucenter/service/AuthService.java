package com.xiaosenho.ucenter.service;

import com.xiaosenho.ucenter.model.dto.AuthParamsDto;
import com.xiaosenho.ucenter.model.dto.XcUserExt;

// 使用策略模式实现统一的登录认证接口
public interface AuthService {
    XcUserExt excute(AuthParamsDto authParamsDto);
}
