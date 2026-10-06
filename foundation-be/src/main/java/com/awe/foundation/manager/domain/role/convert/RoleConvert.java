package com.awe.foundation.manager.domain.role.convert;

import com.awe.foundation.manager.domain.role.entity.Role;
import com.awe.foundation.manager.domain.role.dto.req.RoleAddReq;
import com.awe.foundation.manager.domain.role.dto.req.RoleUpdateReq;
import com.awe.foundation.manager.domain.role.dto.req.RoleReq;
import com.awe.foundation.manager.domain.role.dto.resp.RoleResp;
import com.awe.foundation.common.convert.BaseConvert;
import org.mapstruct.Mapper;

/**
 * 系统角色转换器
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@Mapper(componentModel = "spring")
public interface RoleConvert extends BaseConvert<Role, RoleAddReq, RoleUpdateReq, RoleReq, RoleResp> {

}
