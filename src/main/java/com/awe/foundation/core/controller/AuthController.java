package com.awe.foundation.core.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.auth.dto.req.LoginReq;
import com.awe.foundation.manager.domain.auth.dto.resp.LoginResp;
import com.awe.foundation.manager.domain.user.convert.UserConvert;
import com.awe.foundation.manager.domain.user.dto.resp.UserResp;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.service.IAuthStrategy;
import com.awe.foundation.manager.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Awe
 * @since 2025/12/10 09:00
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Resource
    private IAuthStrategy authStrategy;

    @PostMapping("/login")
    @SaIgnore
    public Result<LoginResp> login(@RequestBody LoginReq loginReq) {
        return Result.success(authStrategy.login(loginReq));
    }

}

