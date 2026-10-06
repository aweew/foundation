package com.awe.foundation.manager.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.manager.domain.auth.dto.req.LoginReq;
import com.awe.foundation.manager.domain.auth.dto.resp.LoginResp;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.service.IAuthStrategy;
import com.awe.foundation.manager.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Awe
 * @since 2025/12/11 13:29
 */
@Service("passwordAuthStrategy")
public class PasswordAuthStrategy implements IAuthStrategy {

    @Resource
    private IUserService userService;

    @Override
    public void validate(LoginReq loginReq) {

    }

    @Override
    public LoginResp login(LoginReq loginReq) {
        String phone = loginReq.getPhone();
        String password = loginReq.getPassword();

        User user = userService.getByPhone(phone);
        if (Objects.isNull(user)) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        // 密码校验失败时统一抛出 ErrorCodeEnum 中的业务错误码
        StpUtil.login(user.getId());

        return LoginResp.builder().accessToken(StpUtil.getTokenValue()).expireIn(StpUtil.getTokenTimeout()).build();
    }

}
