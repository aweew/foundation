package com.awe.foundation.manager.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.constant.enums.StatusEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.manager.domain.auth.dto.req.ChangePasswordReq;
import com.awe.foundation.manager.domain.auth.dto.resp.UserInfoResp;
import com.awe.foundation.manager.domain.menu.dto.resp.MenuResp;
import com.awe.foundation.manager.domain.role.dto.resp.RoleResp;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.mapper.UserMapper;
import com.awe.foundation.manager.service.IMenuService;
import com.awe.foundation.manager.service.IRoleService;
import com.awe.foundation.manager.service.IUserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 系统用户服务实现类
 *
 * @author Awe
 * @since 2025-12-10 16:03:20
 */
@Service("userService")
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private IRoleService roleService;

    @Resource
    private IMenuService menuService;

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Override
    public User getByPhone(String phone) {
        return this.baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getPhone, phone), false);
    }

    /**
     * 查询用户登录后的权限信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @Override
    public UserInfoResp getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            return null;
        }
        if (!StatusEnum.ENABLE.equals(user.getStatus())) {
            throw new BusinessException(ErrorCodeEnum.USER_DISABLED);
        }

        UserInfoResp userInfoResp = new UserInfoResp();
        BeanUtils.copyProperties(user, userInfoResp);

        // 查询角色
        List<RoleResp> roles = roleService.listByUserId(userId);
        userInfoResp.setRoles(roles);

        // 查询菜单
        List<MenuResp> menus = menuService.listAllMenusByUserId(userId);
        userInfoResp.setMenus(menus);

        // 查询权限
        List<String> permissions = menuService.listPermissionsByUserId(userId);
        userInfoResp.setPermissions(permissions);

        return userInfoResp;
    }

    /**
     * 修改用户密码
     *
     * @param userId 用户 ID
     * @param req    修改密码请求
     */
    @Override
    public void changePassword(Long userId, ChangePasswordReq req) {
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }
        boolean passwordMatched;
        try {
            passwordMatched = StringUtils.isNotBlank(user.getPassword())
                    && BCrypt.checkpw(req.getOldPassword(), user.getPassword());
        } catch (IllegalArgumentException exception) {
            passwordMatched = false;
        }
        if (!passwordMatched) {
            throw new BusinessException(ErrorCodeEnum.OLD_PASSWORD_ERROR);
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
        }
        user.setPassword(BCrypt.hashpw(req.getNewPassword()));
        if (!this.updateById(user)) {
            throw new BusinessException(ErrorCodeEnum.OPTIMISTIC_LOCK_CONFLICT);
        }
    }

}
