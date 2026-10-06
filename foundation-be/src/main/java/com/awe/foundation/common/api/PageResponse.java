package com.awe.foundation.common.api;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应对象
 *
 * @author Awe
 * @date 2023/4/6 13:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PageResponse<T> {

    /**
     * 列表数据
     */
    private List<T> records = Collections.emptyList();

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总数
     */
    private Long total;

    /**
     * 创建空分页响应
     *
     * @param pageRequest 分页请求
     * @param <R> 响应类型
     * @param <T> 分页请求类型
     * @return 空分页响应
     */
    public static <R, T> PageResponse<R> empty(PageRequest pageRequest) {
        PageResponse<R> response = new PageResponse<>();
        response.setCurrent(pageRequest.getCurrent());
        response.setSize(pageRequest.getSize());
        response.setTotal(0L);
        return response;
    }

    /**
     * 根据分页结果创建空数据响应
     *
     * @param page 分页结果
     * @param <R> 响应类型
     * @param <T> 数据类型
     * @return 空分页响应
     */
    public static <R, T> PageResponse<R> empty(IPage<T> page) {
        PageResponse<R> response = new PageResponse<>();
        response.setCurrent(page.getCurrent());
        response.setSize(page.getSize());
        response.setTotal(page.getTotal());
        return response;
    }

    /**
     * 根据已有分页响应创建空记录响应
     *
     * @param clazz 响应类型
     * @param r 原分页响应
     * @param <R> 响应类型
     * @return 空记录响应
     */
    public static <R> PageResponse<R> createEmpty(Class<R> clazz, PageResponse r) {
        PageResponse<R> response = new PageResponse<>();
        response.setCurrent(r.getCurrent());
        response.setSize(r.getSize());
        response.setTotal(r.getTotal());
        List<R> records = new ArrayList<>();
        response.setRecords(records);
        return response;
    }

    /**
     * 转换分页记录
     *
     * @param page 分页结果
     * @param dateProcessor 记录转换器
     * @param <R> 响应类型
     * @param <T> 数据类型
     * @return 转换后的分页响应
     */
    public static <R, T> PageResponse<R> create(IPage<T> page, DataProcessor<R, T> dateProcessor) {
        PageResponse<R> response = new PageResponse<>();
        response.setCurrent(page.getCurrent());
        response.setSize(page.getSize());
        response.setTotal(page.getTotal());
        List<R> records = new ArrayList<>();
        if (CollUtil.isNotEmpty(page.getRecords())) {
            for (T record : page.getRecords()) {
                R data = dateProcessor.process(record);
                records.add(data);
            }
        }
        response.setRecords(records);
        return response;
    }

    @FunctionalInterface
    public interface DataProcessor<R, T> {
        R process(T data);
    }

}
