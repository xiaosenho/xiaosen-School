package com.xiaosenho.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosenho.ucenter.mapper.XcUserMapper;
import com.xiaosenho.ucenter.mapper.XcUserRoleMapper;
import com.xiaosenho.ucenter.model.dto.AuthParamsDto;
import com.xiaosenho.ucenter.model.dto.XcUserExt;
import com.xiaosenho.ucenter.model.po.XcUser;
import com.xiaosenho.ucenter.model.po.XcUserRole;
import com.xiaosenho.ucenter.service.AuthService;
import com.xiaosenho.ucenter.service.WxApiService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author: 作者
 * @create: 2025-02-23 15:23
 * @Description: 微信认证具体实现，在二维码登录回调时已经自动注册用户，只需要根据用户名查询用户信息即可
 */
@Service("wx_authservice")
@Slf4j
public class WXAuthServiceImpl implements AuthService, WxApiService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcUserRoleMapper xcUserRoleMapper;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    WxApiService currentProxy;

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    @Override
    public XcUserExt excute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if(user==null){
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        //TODO 获取用户权限
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user,xcUserExt);
        return xcUserExt;
    }

    @Transactional
    @Override
    public XcUser wxAuth(String code) {
        //收到code调用微信接口申请access_token
        Map<String, String> access_token_map = getAccess_token(code);
        if(access_token_map==null){
            return null;
        }
        //获取用户信息
        String openid = access_token_map.get("openid");
        String access_token = access_token_map.get("access_token");
        //拿access_token查询用户信息
        Map<String, String> userinfo = getUserinfo(access_token, openid);
        String unionid = userinfo.get("unionid");//用户唯一标识

        //根据unionid查询数据库表
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getWxUnionid, unionid));
        if(xcUser!=null){ //用户存在
            return xcUser;
        }
        //用户不存在则注册用户
        xcUser = currentProxy.saveXcUser(userinfo);

        return xcUser;
    }

    /**
     * 如果用户不存在则注册用户
     * @param userinfo
     * @return
     */
    @Transactional
    @Override
    public XcUser saveXcUser(Map<String, String> userinfo) {
        String unionid = userinfo.get("unionid");//用户唯一标识
        XcUser xcUser = new XcUser();

        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        String nickname = userinfo.get("nickname");//用户昵称
        String headimgurl = userinfo.get("headimgurl");//用户头像地址
        String sex = String.valueOf(userinfo.get("sex"));//用户性别

        //注册用户到用户表
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(nickname);
        xcUser.setName(nickname);
        xcUser.setUserpic(headimgurl);
        xcUser.setSex(sex);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);

        //注册用户到用户角色表
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }

    /**
     * 根据授权码获取微信令牌
     * @param code
     * @return
     */
    private Map<String,String> getAccess_token(String code) {

        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, appid, secret, code);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String,String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }

    /**
     * 根据微信令牌获取用户信息
     * @param access_token
     * @param openid
     * @return
     */
    private Map<String,String> getUserinfo(String access_token,String openid) {

        String wxUrl_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, access_token,openid);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        //防止乱码进行转码
        String result = new     String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String,String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }


}
