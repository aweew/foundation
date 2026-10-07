package com.awe.foundation.manager.domain.role.dto.req;

import com.awe.foundation.common.constant.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色更新请求对象
 *
 * @author Awe
 * @since 2025-12-15 13:11:22
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 142247573403847620L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 是否系统内置角色
     */
    private Boolean isSystem;

    /**
     * 角色描述
     */
    private String remark;

    /**
     * 状态（1启用，2禁用）
     */
    private StatusEnum status;

}
