package com.awe.foundation.manager.service.impl;

import com.awe.foundation.manager.domain.role.dto.resp.RoleResp;
import com.awe.foundation.manager.domain.role.entity.Role;
import com.awe.foundation.manager.mapper.RoleMapper;
import com.awe.foundation.manager.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统角色服务实现类
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@Service("roleService")
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    /**
     * 查询用户关联角色
     *
     * @param userId 用户 ID
     * @return 角色列表
     */
    @Override
    public List<RoleResp> listByUserId(Long userId) {
        return this.baseMapper.listByUserId(userId);
    }

    /**
     * 查询用户角色编码
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    @Override
    public List<String> listCodesByUserId(Long userId) {
        return this.baseMapper.listCodesByUserId(userId);
    }

}
