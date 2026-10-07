package com.awe.foundation.manager.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.constant.enums.StatusEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.manager.domain.auth.dto.req.LoginReq;
import com.awe.foundation.manager.domain.auth.dto.resp.LoginResp;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.service.IAuthStrategy;
import com.awe.foundation.manager.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Awe
 * @since 2025/12/11 13:29
 */
@Service("passwordAuthStrategy")
public class PasswordAuthStrategy implements IAuthStrategy {

    @Resource
    private IUserService userService;

    @Resource
    private HttpServletRequest request;

    @Override
    public void validate(LoginReq loginReq) {
        if (Objects.isNull(loginReq) || StringUtils.isBlank(loginReq.getPhone())
                || StringUtils.isBlank(loginReq.getPassword())) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp login(LoginReq loginReq) {
        String phone = loginReq.getPhone();
        String password = loginReq.getPassword();

        User user = userService.getByPhone(phone);
        if (Objects.isNull(user)) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        if (!StatusEnum.ENABLE.equals(user.getStatus())) {
            throw new BusinessException(ErrorCodeEnum.USER_DISABLED);
        }

        String storedPassword = user.getPassword();
        if (StringUtils.isBlank(password) || StringUtils.isBlank(storedPassword)) {
            throw new BusinessException(ErrorCodeEnum.PASSWORD_ERROR);
        }

        boolean passwordMatched;
        try {
            passwordMatched = BCrypt.checkpw(password, storedPassword);
        } catch (IllegalArgumentException exception) {
            passwordMatched = false;
        }
        if (!passwordMatched) {
            throw new BusinessException(ErrorCodeEnum.PASSWORD_ERROR);
        }

        StpUtil.login(user.getId());
        user.setLastLoginIp(request.getRemoteAddr());
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        return LoginResp.builder().accessToken(StpUtil.getTokenValue()).expireIn(StpUtil.getTokenTimeout()).build();
    }

}
