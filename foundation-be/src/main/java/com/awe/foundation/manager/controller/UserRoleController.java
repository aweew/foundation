package com.awe.foundation.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.userRole.convert.UserRoleConvert;
import com.awe.foundation.manager.domain.userRole.dto.req.UserRoleAddReq;
import com.awe.foundation.manager.domain.userRole.dto.resp.UserRoleResp;
import com.awe.foundation.manager.domain.userRole.entity.UserRole;
import com.awe.foundation.manager.service.IUserRoleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户角色关联管理
 */
@RestController
@RequestMapping("/sys/user-role")
public class UserRoleController {

    @Resource
    private IUserRoleService userRoleService;

    @Resource
    private UserRoleConvert userRoleConvert;

    /**
     * 查询用户角色关联
     *
     * @param userId 用户 ID
     * @return 用户角色关联列表
     */
    @GetMapping("/user/{userId}")
    @SaCheckPermission("sys:user-role:list")
    public Result<List<UserRoleResp>> listByUserId(@PathVariable("userId") Long userId) {
        List<UserRole> userRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                .eq(UserRole::getUserId, userId));
        return Result.success(userRoles, userRoleConvert::toRespList);
    }

    /**
     * 为用户分配角色
     *
     * @param req 用户角色关联请求
     * @return 操作结果
     */
    @PostMapping
    @SaCheckPermission("sys:user-role:save")
    public Result<Void> assign(@Valid @RequestBody UserRoleAddReq req) {
        userRoleService.assign(req);
        return Result.success();
    }

    /**
     * 删除用户角色关联
     *
     * @param id 关联 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:user-role:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userRoleService.removeById(id);
        return Result.success();
    }

}
