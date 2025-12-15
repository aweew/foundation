package com.awe.foundation.manager.controller;

import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.userRole.convert.UserRoleConvert;
import com.awe.foundation.manager.domain.userRole.dto.req.UserRoleAddReq;
import com.awe.foundation.manager.domain.userRole.dto.req.UserRoleReq;
import com.awe.foundation.manager.domain.userRole.dto.req.UserRoleUpdateReq;
import com.awe.foundation.manager.domain.userRole.dto.resp.UserRoleResp;
import com.awe.foundation.manager.domain.userRole.entity.UserRole;
import com.awe.foundation.manager.service.IUserRoleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 系统用户角色管理
 *
 * @author Awe
 * @since 2025-12-11 16:45:01
 */
@RestController
@RequestMapping("/userRole")
public class UserRoleController {

    @Resource
    private IUserRoleService userRoleService;

    @Resource
    private UserRoleConvert userRoleConvert;

    /**
     * 根据筛选条件获取系统用户角色列表(分页)
     *
     * @param pageReq 分页对象
     * @param req     筛选条件
     * @return 查询结果
     */
    @GetMapping("/page")
    public Result<PageResponse<UserRoleResp>> page(Page<UserRole> pageReq, UserRoleReq req) {
        UserRole userRole = userRoleConvert.toEntity(req);
        Page<UserRole> page = this.userRoleService.page(pageReq, Wrappers.lambdaQuery(userRole));
        return Result.success(page, userRoleConvert::toResp);
    }

    /**
     * 根据筛选条件获取系统用户角色列表(全量)
     *
     * @param req 筛选条件
     * @return 查询结果
     */
    @GetMapping("/list")
    public Result<List<UserRoleResp>> list(UserRoleReq req) {
        UserRole userRole = userRoleConvert.toEntity(req);
        List<UserRole> list = userRoleService.list(Wrappers.lambdaQuery(userRole));
        return Result.success(list, userRoleConvert::toRespList);
    }

    /**
     * 根据id获取系统用户角色
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    public Result<UserRoleResp> getById(@PathVariable("id") Long id) {
        UserRole userRole = this.userRoleService.getById(id);
        if (Objects.nonNull(userRole)) {
            return Result.success(userRole, userRoleConvert::toResp);
        }
        return Result.success();
    }

    /**
     * 新增系统用户角色
     *
     * @param req 新增对象
     * @return 新增结果
     */
    @PostMapping
    public Result<Void> save(@Valid @RequestBody UserRoleAddReq req) {
        this.userRoleService.save(userRoleConvert.addToEntity(req));
        return Result.success();
    }

    /**
     * 更新系统用户角色
     *
     * @param req 更新对象
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserRoleUpdateReq req) {
        UserRole userRole = this.userRoleService.getById(req.getId());
        if (Objects.nonNull(userRole)) {
            userRoleConvert.updateToEntity(req, userRole);
            this.userRoleService.updateById(userRole);
        }
        return Result.success();
    }

    /**
     * 删除系统用户角色
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable("id") Long id) {
        this.userRoleService.removeById(id);
        return Result.success();
    }

}
