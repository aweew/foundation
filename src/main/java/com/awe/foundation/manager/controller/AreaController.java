package com.awe.foundation.manager.controller;

import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.Area;
import com.awe.foundation.manager.domain.req.AreaAddReq;
import com.awe.foundation.manager.domain.req.AreaReq;
import com.awe.foundation.manager.domain.req.AreaUpdateReq;
import com.awe.foundation.manager.domain.resp.AreaResp;
import com.awe.foundation.manager.service.IAreaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 区域管理
 *
 * @author Awe
 * @since 2025-09-09 15:09:21
 */
@RestController
@RequestMapping("/area")
public class AreaController {

    @Resource
    private IAreaService areaService;

    /**
     * 根据筛选条件获取区域列表(分页)
     *
     * @param pageRequest 分页对象
     * @param req         筛选条件
     * @return 查询结果
     */
    @GetMapping("/page")
    public Result<PageResponse<AreaResp>> page(Page pageRequest, AreaReq req) {
        Area area = Area.builder().build();
        BeanUtils.copyProperties(req, area);
        LambdaQueryWrapper<Area> qw = new LambdaQueryWrapper<>(area);
        Page<Area> page = this.areaService.page(pageRequest, qw);
        PageResponse<AreaResp> pageResponse = PageResponse.create(page, Area::convert);
        return Result.success(pageResponse);
    }

    /**
     * 根据筛选条件获取区域列表(全量)
     *
     * @param req 筛选条件
     * @return 查询结果
     */
    @GetMapping("/list")
    public Result<List<AreaResp>> list(AreaReq req) {
        Area area = Area.builder().build();
        BeanUtils.copyProperties(req, area);
        LambdaQueryWrapper<Area> qw = new LambdaQueryWrapper<>(area);
        return Result.success(this.areaService.list(qw).stream().map(Area::convert).toList());
    }

    /**
     * 根据id获取区域
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    public Result<AreaResp> getById(@PathVariable("id") Long id) {
        Area area = this.areaService.getById(id);
        if (Objects.nonNull(area)) {
            return Result.success(area.convert());
        }
        return Result.success();
    }

    /**
     * 新增区域
     *
     * @param req 新增对象
     * @return 新增结果
     */
    @PostMapping
    public Result<Void> save(@Valid @RequestBody AreaAddReq req) {
        Area area = Area.builder().build();
        BeanUtils.copyProperties(req, area);
        area.setCreateTime(LocalDateTime.now());
        area.setUpdateTime(LocalDateTime.now());
        this.areaService.save(area);
        return Result.success();
    }

    /**
     * 更新区域
     *
     * @param req 更新对象
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AreaUpdateReq req) {
        Area area = Area.builder().build();
        BeanUtils.copyProperties(req, area);
        area.setUpdateTime(LocalDateTime.now());
        this.areaService.updateById(area);
        return Result.success();
    }

    /**
     * 删除区域
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable("id") Long id) {
        this.areaService.removeById(id);
        return Result.success();
    }

}
