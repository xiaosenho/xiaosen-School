package com.xiaosenho.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosenho.ucenter.mapper.XcUserMapper;
import com.xiaosenho.ucenter.model.po.XcUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: 作者
 * @create: 2025-02-23 14:13
 * @Description:
 */
@Service
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private XcUserMapper xcUserMapper;

    //实现UserDetailsService接口方法，根据用户名查询数据库用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        // 如果查询不到，返回空
        if (xcUser == null) {
            return null;
        }
        // 获取密码，由Spring Security框架调用，自动进行密码比对
        String password = xcUser.getPassword();
        // TODO 获取权限
        // 扩展UserDetails对象，封装用户信息
        xcUser.setPassword(null);
        String json = JSON.toJSON(xcUser).toString();
        // 封装成UserDetails对象返回
        return User.withUsername(json).password(password).authorities("p1").build();
    }
}
