package com.awe.foundation.manager.controller;

import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.roleMenu.convert.RoleMenuConvert;
import com.awe.foundation.manager.domain.roleMenu.dto.req.RoleMenuAddReq;
import com.awe.foundation.manager.domain.roleMenu.dto.req.RoleMenuReq;
import com.awe.foundation.manager.domain.roleMenu.dto.req.RoleMenuUpdateReq;
import com.awe.foundation.manager.domain.roleMenu.dto.resp.RoleMenuResp;
import com.awe.foundation.manager.domain.roleMenu.entity.RoleMenu;
import com.awe.foundation.manager.service.IRoleMenuService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 系统角色权限管理
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@RestController
@RequestMapping("/roleMenu")
public class RoleMenuController {

    @Resource
    private IRoleMenuService roleMenuService;

    @Resource
    private RoleMenuConvert roleMenuConvert;

    /**
     * 根据筛选条件获取系统角色权限列表(分页)
     *
     * @param pageReq 分页对象
     * @param req     筛选条件
     * @return 查询结果
     */
    @GetMapping("/page")
    public Result<PageResponse<RoleMenuResp>> page(Page<RoleMenu> pageReq, RoleMenuReq req) {
        RoleMenu roleMenu = roleMenuConvert.toEntity(req);
        Page<RoleMenu> page = this.roleMenuService.page(pageReq, Wrappers.lambdaQuery(roleMenu));
        return Result.success(page, roleMenuConvert::toResp);
    }

    /**
     * 根据筛选条件获取系统角色权限列表(全量)
     *
     * @param req 筛选条件
     * @return 查询结果
     */
    @GetMapping("/list")
    public Result<List<RoleMenuResp>> list(RoleMenuReq req) {
        RoleMenu roleMenu = roleMenuConvert.toEntity(req);
        List<RoleMenu> list = roleMenuService.list(Wrappers.lambdaQuery(roleMenu));
        return Result.success(list, roleMenuConvert::toRespList);
    }

    /**
     * 根据id获取系统角色权限
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    public Result<RoleMenuResp> getById(@PathVariable("id") Long id) {
        RoleMenu roleMenu = this.roleMenuService.getById(id);
        if (Objects.nonNull(roleMenu)) {
            return Result.success(roleMenu, roleMenuConvert::toResp);
        }
        return Result.success();
    }

    /**
     * 新增系统角色权限
     *
     * @param req 新增对象
     * @return 新增结果
     */
    @PostMapping
    public Result<Void> save(@Valid @RequestBody RoleMenuAddReq req) {
        this.roleMenuService.save(roleMenuConvert.addToEntity(req));
        return Result.success();
    }

    /**
     * 更新系统角色权限
     *
     * @param req 更新对象
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody RoleMenuUpdateReq req) {
        RoleMenu roleMenu = this.roleMenuService.getById(req.getId());
        if (Objects.nonNull(roleMenu)) {
            roleMenuConvert.updateToEntity(req, roleMenu);
            this.roleMenuService.updateById(roleMenu);
        }
        return Result.success();
    }

    /**
     * 删除系统角色权限
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable("id") Long id) {
        this.roleMenuService.removeById(id);
        return Result.success();
    }

}
