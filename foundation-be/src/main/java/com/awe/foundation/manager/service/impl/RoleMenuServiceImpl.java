package com.awe.foundation.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awe.foundation.manager.domain.roleMenu.entity.RoleMenu;
import com.awe.foundation.manager.mapper.RoleMenuMapper;
import com.awe.foundation.manager.service.IRoleMenuService;
import org.springframework.stereotype.Service;

/**
 * 系统角色权限服务实现类
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@Service("roleMenuService")
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService {

}
