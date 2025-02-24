package com.xiaosenho.auth.controller;

import com.xiaosenho.ucenter.model.po.XcUser;
import com.xiaosenho.ucenter.service.WxApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author: 作者
 * @create: 2025-02-23 19:44
 * @Description:
 */
@Controller
@Slf4j
public class WXLoginController {
    @Resource
    private WxApiService wxApiService;

    // 微信扫码登录回调接口，传入授权码
    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        XcUser xcUser = wxApiService.wxAuth(code);
        if(xcUser==null){
            return "redirect:https://www.51xuecheng.cn/error.html";
        }
        //根据用户信息重定向到前端页面，发起正式的登录请求，获取服务端令牌
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
    }

}
