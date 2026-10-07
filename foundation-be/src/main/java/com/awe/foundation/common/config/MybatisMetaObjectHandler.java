package com.awe.foundation.common.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus审计字段自动填充处理器
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * 填充新增审计字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        fillIfPresent(metaObject, "createTime", now);
        fillIfPresent(metaObject, "updateTime", now);
        Long userId = currentUserId();
        fillIfPresent(metaObject, "createUserId", userId);
        fillIfPresent(metaObject, "updateUserId", userId);
    }

    /**
     * 填充更新审计字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        setIfPresent(metaObject, "updateTime", LocalDateTime.now());
        setIfPresent(metaObject, "updateUserId", currentUserId());
    }

    /**
     * 仅填充实体中存在的字段
     *
     * @param metaObject 元对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    private void fillIfPresent(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasSetter(fieldName) && metaObject.getValue(fieldName) == null) {
            metaObject.setValue(fieldName, value);
        }
    }

    /**
     * 覆盖填充实体中的审计字段
     *
     * @param metaObject 元对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    private void setIfPresent(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasSetter(fieldName)) {
            metaObject.setValue(fieldName, value);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @return 用户ID，未登录时返回null
     */
    private Long currentUserId() {
        return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
    }
}
