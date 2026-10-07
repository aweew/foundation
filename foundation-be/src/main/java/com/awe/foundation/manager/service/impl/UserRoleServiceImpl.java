package com.awe.foundation.manager.service.impl;

import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.constant.enums.StatusEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.manager.domain.role.entity.Role;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.domain.userRole.dto.req.UserRoleAddReq;
import com.awe.foundation.manager.domain.userRole.entity.UserRole;
import com.awe.foundation.manager.mapper.UserRoleMapper;
import com.awe.foundation.manager.service.IRoleService;
import com.awe.foundation.manager.service.IUserRoleService;
import com.awe.foundation.manager.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 系统用户角色服务实现类
 *
 * @author Awe
 * @since 2025-12-11 16:45:02
 */
@Service("userRoleService")
@Transactional(rollbackFor = Exception.class)
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

    @Resource
    private IUserService userService;

    @Resource
    private IRoleService roleService;

    /**
     * 校验并新增用户角色关联
     *
     * @param req 用户角色关联请求
     */
    @Override
    public void assign(UserRoleAddReq req) {
        User user = userService.getById(req.getUserId());
        Role role = roleService.getById(req.getRoleId());
        if (Objects.isNull(user) || Objects.isNull(role)) {
            throw new BusinessException(ErrorCodeEnum.DATA_NOT_FOUND);
        }
        if (!StatusEnum.ENABLE.equals(user.getStatus()) || !StatusEnum.ENABLE.equals(role.getStatus())) {
            throw new BusinessException(ErrorCodeEnum.DATA_DISABLED);
        }
        UserRole exists = this.baseMapper.selectAny(req.getUserId(), req.getRoleId());
        if (Objects.nonNull(exists)) {
            if (Boolean.TRUE.equals(exists.getIsDelete())) {
                this.baseMapper.restore(exists.getId());
            }
            return;
        }
        this.save(UserRole.builder().userId(req.getUserId()).roleId(req.getRoleId()).build());
    }

}
