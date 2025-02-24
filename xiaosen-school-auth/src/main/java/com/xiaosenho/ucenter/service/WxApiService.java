package com.xiaosenho.ucenter.service;

import com.xiaosenho.ucenter.model.po.XcUser;

import java.util.Map;

public interface WxApiService {
    /**
     * 根据微信授权码向微信任务服务器获取令牌，并根据令牌获取用户信息
     * @param code
     * @return
     */
    XcUser wxAuth(String code);

    /**
     * 根据用户信息注册用户,提到接口层用于代理调用，避免事务失效
     * @param userinfo
     * @return
     */
    XcUser saveXcUser(Map<String, String> userinfo);
}
