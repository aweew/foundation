package com.awe.foundation.manager.domain;

import com.awe.foundation.common.convert.ConvertTo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.awe.foundation.manager.domain.resp.AreaResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区域实体类
 *
 * @author Awe
 * @since 2025-09-09 15:09:22
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName("base_area")
public class Area implements Serializable, ConvertTo<AreaResp> {

    @Serial
    private static final long serialVersionUID = -77332475216483029L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域编码
     */
    private String code;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 别名
     */
    private String nameAlias;

    /**
     * 父级code
     */
    private String parentCode;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 层级名称链，例如：|-1|3|22|，通过“|”分割，开始和结尾需要加“|”，防止前缀相同问题
     */
    private String chain;

    /**
     * 层级名称链，例如：|-1|3|22|，通过“|”分割，开始和结尾需要加“|”，防止前缀相同问题
     */
    private String chainCode;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 经度
     */
    private BigDecimal lon;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 创建人id
     */
    private Long createUserId;

    /**
     * 更新人id
     */
    private Long updateUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除状态：0=未删除，1=已删除
     */
    private Integer deleted;

    @Override
    public AreaResp convert() {
        AreaResp resp = AreaResp.builder().build();
        BeanUtils.copyProperties(this, resp);
        return resp;
    }

}
