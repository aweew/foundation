package com.awe.foundation.manager.service;

import com.awe.foundation.manager.domain.role.dto.resp.RoleResp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.awe.foundation.manager.domain.role.entity.Role;

import java.util.List;

/**
 * 系统角色服务接口
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
public interface IRoleService extends IService<Role> {

    List<RoleResp> listByUserId(Long userId);

}
