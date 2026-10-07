package com.awe.foundation.manager.domain.roleMenu.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色权限新增请求对象
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RoleMenuAddReq implements Serializable {

    @Serial
    private static final long serialVersionUID = -82774415658532767L;

    /**
     * 角色id（关联sys_user表）
     */
    @NotNull(message = "角色不能为空")
    private Long roleId;

    /**
     * 权限id（关联sys_menu表）
     */
    @NotNull(message = "权限不能为空")
    private Long menuId;

}
