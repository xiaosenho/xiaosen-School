package com.xiaosenho.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosenho.ucenter.mapper.XcMenuMapper;
import com.xiaosenho.ucenter.mapper.XcUserMapper;
import com.xiaosenho.ucenter.model.dto.AuthParamsDto;
import com.xiaosenho.ucenter.model.dto.XcUserExt;
import com.xiaosenho.ucenter.model.po.XcMenu;
import com.xiaosenho.ucenter.model.po.XcUser;
import com.xiaosenho.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: 作者
 * @create: 2025-02-23 14:13
 * @Description:
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private XcUserMapper xcUserMapper;
    @Resource
    private XcMenuMapper xcMenuMapper;
    @Resource
    private ApplicationContext context;

    //实现UserDetailsService接口方法，根据用户名查询数据库用户信息
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        // 传入数据解析成AuthParamsDto
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式不对");
        }
        // 根据认证方式动态注入具体实现bean
        String authType = authParamsDto.getAuthType()+"_authservice";
        AuthService service = context.getBean(authType,AuthService.class);
        XcUserExt userExt = service.excute(authParamsDto);

        // 获取用户权限列表
        List<XcMenu> xcMenuList = xcMenuMapper.selectPermissionByUserId(userExt.getId());
        List<String> permissions = xcMenuList.stream().map(XcMenu::getCode).collect(Collectors.toList());
        userExt.setPermissions(permissions);
        // 带参toArray指定数组类型,否则默认为Object类型
        String[] permissionsArray = permissions.toArray(new String[0]);

        // 封装成UserDetails对象返回
        String password = userExt.getPassword();
        userExt.setPassword(null);
        String json = JSON.toJSON(userExt).toString();//最终JWT根据我们封装的用户信息生成token
        return User.withUsername(json).password(password).authorities(permissionsArray).build();
    }
}
