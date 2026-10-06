package com.awe.foundation.common.api;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private Long current = 1L;

    /**
     * pageSize
     */
    @Min(value = 1)
    @Max(value = 200)
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否查询数量
     */
    private boolean isSearchCount = true;

    /**
     * 排序类型，desc/asc
     */
    private String orderType;

    @Schema(hidden = true)
    private List<OrderItem> _orderItems;

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
     * @param <T> 数据类型
     * @return 分页对象
     */
    public <T> Page<T> createPage(Class<T> requireType) {
        Page<T> result = new Page<>(this.current, this.size, this.isSearchCount);
        if (StrUtil.isNotEmpty(this.orderBy) && StrUtil.isNotEmpty(this.orderType)) {
            String[] orderByArr = this.orderBy.split(",");
            String[] orderTypeArr = this.orderType.split(",");

            for (int i = 0; i < orderByArr.length; ++i) {
                String sortBy = orderByArr[i];
                String sortType = orderTypeArr[i];
                OrderItem orderItem = new OrderItem();
                orderItem.setAsc("asc".equalsIgnoreCase(sortType));
                orderItem.setColumn(sortBy);
                result.addOrder(orderItem);
            }
        }
        if (CollUtil.isNotEmpty(this._orderItems)) {
            result.addOrder(this._orderItems);
        }
        return result;
    }

    /**
     * 添加排序项
     *
     * @param name 排序字段
     * @param asc 是否升序
     */
    public void addOrderItem(String name, boolean asc) {
        if (null == _orderItems) {
            _orderItems = new ArrayList<>();
        }
        OrderItem orderItem = OrderItem.asc(name);
        _orderItems.add(orderItem);
    }

    /**
     * 转换为分页响应对象
     *
     * @param clazz 响应类型
     * @param <T> 响应类型
     * @return 分页响应对象
     */
    public <T extends PageResponse<?>> T toPageResp(Class<T> clazz) {
        this.setSearchCount(true);
        T t = ReflectUtil.newInstance(clazz);
        BeanUtil.copyProperties(this, t);
        return t;
    }

}
