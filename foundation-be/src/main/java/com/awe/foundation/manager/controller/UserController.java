package com.awe.foundation.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.secure.BCrypt;
import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.manager.domain.user.convert.UserConvert;
import com.awe.foundation.manager.domain.user.dto.req.UserAddReq;
import com.awe.foundation.manager.domain.user.dto.req.UserReq;
import com.awe.foundation.manager.domain.user.dto.req.UserUpdateReq;
import com.awe.foundation.manager.domain.user.dto.resp.UserResp;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.service.IUserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 系统用户管理
 *
 * @author Awe
 * @since 2025-12-10 16:03:46
 */
@RestController
@RequestMapping("/sys/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private UserConvert userConvert;

    /**
     * 根据筛选条件获取系统用户列表(分页)
     *
     * @param pageReq 分页对象
     * @param req     筛选条件
     * @return 查询结果
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:user:list")
    public Result<PageResponse<UserResp>> page(Page<User> pageReq, UserReq req) {
        User user = userConvert.toEntity(req);
        Page<User> page = this.userService.page(pageReq, Wrappers.lambdaQuery(user));
        return Result.success(page, userConvert::toResp);
    }

    /**
     * 根据筛选条件获取系统用户列表(全量)
     *
     * @param req 筛选条件
     * @return 查询结果
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:user:list")
    public Result<List<UserResp>> list(UserReq req) {
        User user = userConvert.toEntity(req);
        List<User> list = userService.list(Wrappers.lambdaQuery(user));
        return Result.success(list, userConvert::toRespList);
    }

    /**
     * 根据id获取系统用户
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    @SaCheckPermission("sys:user:view")
    public Result<UserResp> getById(@PathVariable("id") Long id) {
        User user = this.userService.getById(id);
        if (Objects.nonNull(user)) {
            return Result.success(user, userConvert::toResp);
        }
        return Result.success();
    }

    /**
     * 新增系统用户
     *
     * @param req 新增对象
     * @return 新增结果
     */
    @PostMapping
    @SaCheckPermission("sys:user:save")
    public Result<Void> save(@Valid @RequestBody UserAddReq req) {
        req.setPassword(BCrypt.hashpw(req.getPassword()));
        this.userService.save(userConvert.addToEntity(req));
        return Result.success();
    }

    /**
     * 更新系统用户
     *
     * @param req 更新对象
     * @return 更新结果
     */
    @PutMapping
    @SaCheckPermission("sys:user:update")
    public Result<Void> update(@Valid @RequestBody UserUpdateReq req) {
        User user = this.userService.getById(req.getId());
        if (Objects.isNull(user)) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        String existingPassword = user.getPassword();
        userConvert.updateToEntity(req, user);
        if (StringUtils.isNotBlank(req.getPassword())) {
            user.setPassword(BCrypt.hashpw(req.getPassword()));
        } else {
            user.setPassword(existingPassword);
        }
        this.userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除系统用户
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:user:delete")
    public Result<Void> deleteById(@PathVariable("id") Long id) {
        if (Objects.isNull(this.userService.getById(id))) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        this.userService.removeById(id);
        return Result.success();
    }

}
