package com.awe.foundation.manager.service;

import com.awe.foundation.manager.domain.userRole.dto.req.UserRoleAddReq;
import com.awe.foundation.manager.domain.userRole.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统用户角色服务接口
 *
 * @author Awe
 * @since 2025-12-11 16:45:02
 */
public interface IUserRoleService extends IService<UserRole> {

    /**
     * 校验并新增用户角色关联
     *
     * @param req 用户角色关联请求
     */
    void assign(UserRoleAddReq req);

}
