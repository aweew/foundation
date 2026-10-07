package com.awe.foundation.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.manager.domain.role.convert.RoleConvert;
import com.awe.foundation.manager.domain.role.dto.req.RoleAddReq;
import com.awe.foundation.manager.domain.role.dto.req.RoleReq;
import com.awe.foundation.manager.domain.role.dto.req.RoleUpdateReq;
import com.awe.foundation.manager.domain.role.dto.resp.RoleResp;
import com.awe.foundation.manager.domain.role.entity.Role;
import com.awe.foundation.manager.service.IRoleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 系统角色管理
 *
 * @author Awe
 * @since 2025-12-11 16:44:59
 */
@RestController
@RequestMapping("/sys/role")
public class RoleController {

    @Resource
    private IRoleService roleService;

    @Resource
    private RoleConvert roleConvert;

    /**
     * 根据筛选条件获取系统角色列表(分页)
     *
     * @param pageReq 分页对象
     * @param req     筛选条件
     * @return 查询结果
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:role:list")
    public Result<PageResponse<RoleResp>> page(Page<Role> pageReq, RoleReq req) {
        Role role = roleConvert.toEntity(req);
        Page<Role> page = this.roleService.page(pageReq, Wrappers.lambdaQuery(role));
        return Result.success(page, roleConvert::toResp);
    }

    /**
     * 根据筛选条件获取系统角色列表(全量)
     *
     * @param req 筛选条件
     * @return 查询结果
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:role:list")
    public Result<List<RoleResp>> list(RoleReq req) {
        Role role = roleConvert.toEntity(req);
        List<Role> list = roleService.list(Wrappers.lambdaQuery(role));
        return Result.success(list, roleConvert::toRespList);
    }

    /**
     * 根据id获取系统角色
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    @SaCheckPermission("sys:role:view")
    public Result<RoleResp> getById(@PathVariable("id") Long id) {
        Role role = this.roleService.getById(id);
        if (Objects.nonNull(role)) {
            return Result.success(role, roleConvert::toResp);
        }
        return Result.success();
    }

    /**
     * 新增系统角色
     *
     * @param req 新增对象
     * @return 新增结果
     */
    @PostMapping
    @SaCheckPermission("sys:role:save")
    public Result<Void> save(@Valid @RequestBody RoleAddReq req) {
        this.roleService.save(roleConvert.addToEntity(req));
        return Result.success();
    }

    /**
     * 更新系统角色
     *
     * @param req 更新对象
     * @return 更新结果
     */
    @PutMapping
    @SaCheckPermission("sys:role:update")
    public Result<Void> update(@Valid @RequestBody RoleUpdateReq req) {
        Role role = this.roleService.getById(req.getId());
        if (Objects.isNull(role)) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(role.getIsSystem()) || "SUPER_ADMIN".equalsIgnoreCase(role.getCode())) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_DATA_PROTECTED);
        }
        roleConvert.updateToEntity(req, role);
        if (!this.roleService.updateById(role)) {
            throw new BusinessException(ErrorCodeEnum.OPTIMISTIC_LOCK_CONFLICT);
        }
        return Result.success();
    }

    /**
     * 删除系统角色
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role:delete")
    public Result<Void> deleteById(@PathVariable("id") Long id) {
        Role role = this.roleService.getById(id);
        if (Objects.isNull(role)) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(role.getIsSystem()) || "SUPER_ADMIN".equalsIgnoreCase(role.getCode())) {
            throw new BusinessException(ErrorCodeEnum.SYSTEM_DATA_PROTECTED);
        }
        this.roleService.removeById(id);
        return Result.success();
    }

}
