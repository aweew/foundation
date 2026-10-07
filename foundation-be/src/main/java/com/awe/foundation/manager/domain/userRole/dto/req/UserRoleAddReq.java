package com.awe.foundation.manager.domain.userRole.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统用户角色新增请求对象
 *
 * @author Awe
 * @since 2025-12-11 16:45:02
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleAddReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 597979471423125417L;

    /**
     * 用户id（关联sys_user表）
     */
    @NotNull(message = "用户不能为空")
    private Long userId;

    /**
     * 角色id（关联sys_user表）
     */
    @NotNull(message = "角色不能为空")
    private Long roleId;

}
