package com.awe.foundation.manager.controller;

import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.area.convert.AreaConvert;
import com.awe.foundation.manager.domain.area.dto.AreaAddReq;
import com.awe.foundation.manager.domain.area.dto.AreaReq;
import com.awe.foundation.manager.domain.area.dto.AreaResp;
import com.awe.foundation.manager.domain.area.dto.AreaUpdateReq;
import com.awe.foundation.manager.domain.area.entity.Area;
import com.awe.foundation.manager.service.IAreaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Resource
    private AreaConvert areaConvert;

    /**
     * 根据筛选条件获取区域列表(分页)
     *
     * @param pageRequest 分页对象
     * @param req         筛选条件
     * @return 查询结果
     */
    @GetMapping("/page")
    public Result<PageResponse<AreaResp>> page(Page<Area> pageRequest, AreaReq req) {
        // 1. Req -> Entity
        Area area = areaConvert.toEntity(req);

        // 2. 构建查询条件
        LambdaQueryWrapper<Area> qw = new LambdaQueryWrapper<>(area);

        // 3. 分页查询
        Page<Area> page = this.areaService.page(pageRequest, qw);

        // 4. 转换成 Resp
        PageResponse<AreaResp> pageResponse = PageResponse.create(page, areaConvert::toResp);

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
        // 1. Req -> Entity
        Area area = areaConvert.toEntity(req);

        // 2. 构建查询条件
        LambdaQueryWrapper<Area> qw = new LambdaQueryWrapper<>(area);

        // 3. 查询列表
        List<Area> list = this.areaService.list(qw);

        // 4. Entity -> Resp
        List<AreaResp> respList = areaConvert.toRespList(list);

        return Result.success(respList);
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
        return Result.success(areaConvert.toResp(area));
    }

    /**
     * 新增区域
     *
     * @param req 新增对象
     * @return 新增结果
     */
    @PostMapping
    public Result<Void> save(@Valid @RequestBody AreaAddReq req) {
        this.areaService.save(areaConvert.toEntity(req));
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
        Area area = this.areaService.getById(req.getId());
        areaConvert.updateEntity(req, area);
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
