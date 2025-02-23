package com.xiaosenho.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosenho.ucenter.feignclient.CheckcodeServiceClient;
import com.xiaosenho.ucenter.mapper.XcUserMapper;
import com.xiaosenho.ucenter.model.dto.AuthParamsDto;
import com.xiaosenho.ucenter.model.dto.XcUserExt;
import com.xiaosenho.ucenter.model.po.XcUser;
import com.xiaosenho.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: 作者
 * @create: 2025-02-23 15:22
 * @Description: 密码模式登录验证
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    @Resource
    private XcUserMapper xcUserMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private CheckcodeServiceClient checkcodeServiceClient;
    @Override
    public XcUserExt excute(AuthParamsDto authParamsDto) {
        // 用户名获取
        String username = authParamsDto.getUsername();
        // 校验验证码
        String key = authParamsDto.getCheckcodekey();
        String checkcode = authParamsDto.getCheckcode();
        // 远程调用
        Boolean verify = checkcodeServiceClient.verify(key, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }
        // 根据用户名查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        // 如果查询不到，返回空
        if (xcUser == null) {
            throw new RuntimeException("用户名或密码不正确");
        }
        // 获取密码，我们屏蔽了spring security的密码验证，由自己实现比对
        String password = xcUser.getPassword();
        // 密码比对
        if (!passwordEncoder.matches(authParamsDto.getPassword(), password)) {
            throw new RuntimeException("用户名或密码不正确");
        }
        XcUserExt xcUserExt = new XcUserExt();
        // TODO 权限查询
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
