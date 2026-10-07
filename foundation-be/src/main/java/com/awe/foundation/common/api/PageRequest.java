package com.awe.foundation.common.api;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.StringUtils;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 分页请求对象
 *
 * @author Awe
 * @date 2023/4/6 13:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4093768915141693974L;

    /**
     * 当前页
     */
    @NotNull
    @Min(value = 1)
    @Builder.Default
    private Long current = 1L;

    /**
     * 每页记录数
     */
    @NotNull
    @Min(value = 1)
    @Max(value = 200)
    @Builder.Default
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否查询数量
     */
    @Builder.Default
    private boolean isSearchCount = true;

    /**
     * 排序类型，desc/asc
     */
    private String orderType;

    /**
     * 服务端补充排序项，不参与请求绑定
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final List<OrderItem> serverOrderItems = new ArrayList<>();

    /**
     * 必须写在getter上，否则无效
     */
    @Schema(hidden = true)
    public boolean isSearchCount() {
        return isSearchCount;
    }

    /**
     * 创建 MyBatis-Plus 分页对象
     *
     * @param requireType 分页对象类型
     * @param <T>         数据类型
     * @return 分页对象
     */
    public <T> Page<T> createPage(Class<T> requireType) {
        if (Objects.isNull(current) || current < 1 || Objects.isNull(size) || size < 1 || size > 200) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
        }
        Page<T> result = new Page<>(this.current, this.size, this.isSearchCount);
        Set<String> allowedOrderColumns = new HashSet<>();
        if (StringUtils.isNotBlank(orderBy) || StringUtils.isNotBlank(orderType)
                || CollUtil.isNotEmpty(serverOrderItems)) {
            TableInfo tableInfo = Objects.isNull(requireType) ? null : TableInfoHelper.getTableInfo(requireType);
            if (Objects.isNull(tableInfo)) {
                throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
            }
            if (StringUtils.isNotBlank(tableInfo.getKeyColumn())) {
                allowedOrderColumns.add(tableInfo.getKeyColumn().toLowerCase(Locale.ROOT));
            }
            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                allowedOrderColumns.add(fieldInfo.getColumn().toLowerCase(Locale.ROOT));

            }

        }
        if (StringUtils.isNotBlank(this.orderBy) || StringUtils.isNotBlank(this.orderType)) {
            if (StringUtils.isBlank(this.orderBy) || StringUtils.isBlank(this.orderType)) {
                throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
            }
            String[] orderByArr = this.orderBy.split(",", -1);
            String[] orderTypeArr = this.orderType.split(",", -1);
            if (orderByArr.length != orderTypeArr.length) {
                throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
            }

            for (int i = 0; i < orderByArr.length; ++i) {
                String sortBy = orderByArr[i].trim().toLowerCase(Locale.ROOT);
                String sortType = orderTypeArr[i].trim();
                if (!allowedOrderColumns.contains(sortBy)
                        || !("asc".equalsIgnoreCase(sortType) || "desc".equalsIgnoreCase(sortType))) {
                    throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setAsc("asc".equalsIgnoreCase(sortType));
                orderItem.setColumn(sortBy);
                result.addOrder(orderItem);
            }
        }
        for (OrderItem orderItem : serverOrderItems) {
            if (!allowedOrderColumns.contains(orderItem.getColumn().toLowerCase(Locale.ROOT))) {
                throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
            }
            result.addOrder(orderItem);

        }
        return result;
    }

    /**
     * 添加排序项
     *
     * @param name 排序字段
     * @param asc  是否升序
     */
    public void addOrderItem(String name, boolean asc) {
        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_ERROR);
        }
        serverOrderItems.add(asc ? OrderItem.asc(name) : OrderItem.desc(name));
    }

    /**
     * 转换为分页响应对象
     *
     * @param clazz 响应类型
     * @param <T>   响应类型
     * @return 分页响应对象
     */
    public <T extends PageResponse<?>> T toPageResp(Class<T> clazz) {
        this.setSearchCount(true);
        T pageResponse = ReflectUtil.newInstance(clazz);
        BeanUtil.copyProperties(this, pageResponse);
        return pageResponse;
    }

}
