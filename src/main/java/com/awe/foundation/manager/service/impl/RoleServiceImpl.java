package com.awe.foundation.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awe.foundation.manager.domain.role.entity.Role;
import com.awe.foundation.manager.mapper.RoleMapper;
import com.awe.foundation.manager.service.IRoleService;
import org.springframework.stereotype.Service;

/**
 * 系统角色服务实现类
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@Service("roleService")
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
