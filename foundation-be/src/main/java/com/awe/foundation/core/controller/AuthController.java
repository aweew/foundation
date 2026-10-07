package com.awe.foundation.core.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.awe.foundation.common.annotation.AuditLog;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.auth.dto.req.ChangePasswordReq;
import com.awe.foundation.manager.domain.auth.dto.req.LoginReq;
import com.awe.foundation.manager.domain.auth.dto.resp.LoginResp;
import com.awe.foundation.manager.domain.auth.dto.resp.UserInfoResp;
import com.awe.foundation.manager.service.IAuthStrategy;
import com.awe.foundation.manager.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Awe
 * @since 2025/12/10 09:00
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Resource
    private IUserService userService;

    @Resource
    private IAuthStrategy authStrategy;

    @PostMapping("/login")
    @SaIgnore
    @AuditLog(logType = "LOGIN", operationName = "用户登录")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq loginReq) {
        authStrategy.validate(loginReq);
        return Result.success(authStrategy.login(loginReq));
    }

    /**
     * 根据id获取系统用户
     *
     * @return 单条数据
     */
    @GetMapping("/userInfo")
    @SaCheckLogin
    public Result<UserInfoResp> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(this.userService.getUserInfo(userId));
    }

    /**
     * 退出当前登录会话
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    @SaCheckLogin
    @AuditLog(logType = "LOGOUT", operationName = "用户退出")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    /**
     * 查询当前登录状态
     *
     * @return 登录状态
     */
    @GetMapping("/isLogin")
    @SaIgnore
    public Result<Boolean> isLogin() {
        return Result.success(StpUtil.isLogin());
    }

    /**
     * 续期当前登录令牌
     *
     * @return 续期后的令牌信息
     */
    @PostMapping("/refresh")
    @SaCheckLogin
    public Result<LoginResp> refresh() {
        StpUtil.renewTimeout(StpUtil.getTokenTimeout());
        return Result.success(LoginResp.builder()
                .accessToken(StpUtil.getTokenValue())
                .expireIn(StpUtil.getTokenTimeout())
                .build());
    }

    /**
     * 修改当前用户密码
     *
     * @param req 修改密码请求
     * @return 操作结果
     */
    @PutMapping("/password")
    @SaCheckLogin
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        this.userService.changePassword(StpUtil.getLoginIdAsLong(), req);
        StpUtil.logout();
        return Result.success();
    }

}
