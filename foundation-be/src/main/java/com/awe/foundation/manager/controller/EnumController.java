package com.awe.foundation.manager.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.EnumCollector;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 枚举管理
 *
 * @author Awe
 * @since 2025/12/9 15:32
 */
@RestController
@RequestMapping("/enum")
public class EnumController {

    @Resource
    private EnumCollector enumCollector;

    /**
     * 获取所有枚举列表，包含英文名称、业务值和中文标签
     *
     * @return 枚举列表，选项字段为 name、value、label
     */
    @GetMapping("/list")
    @SaCheckLogin
    public Result<?> listAllEnums() {
        return Result.success(enumCollector.getAllEnums());
    }

}
