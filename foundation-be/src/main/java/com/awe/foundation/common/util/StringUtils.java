package com.awe.foundation.common.util;

import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Objects;

/**
 * 字符串工具类
 *
 * @author Awe
 * @since 2025/9/9 14:34
 */
public class StringUtils extends StrUtil {

    public static final String SEPARATOR = ",";

    /**
     * 判断字符串是否忽略大小写前缀匹配
     *
     * @param value 目标字符串
     * @param prefix 前缀字符串
     * @return 是否匹配
     */
    public static boolean startsWithIgnoreCase(String value, String prefix) {
        if (Objects.isNull(value) || Objects.isNull(prefix)) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * 判断 url 是否匹配排除列表
     * 支持:
     * 1. 完整匹配
     * 2. 末尾 * 前缀匹配，如 /api/user/*
     * 3. 正则匹配
     */
    public static boolean matches(String url, List<String> patterns) {
        if (Objects.isNull(patterns) || patterns.isEmpty()) {
            return false;
        }

        for (String pattern : patterns) {
            if (Objects.isNull(pattern) || pattern.isEmpty()) {
                continue;
            }

            // 1. 完整匹配
            if (pattern.equals(url)) {
                return true;
            }

            // 2. 前缀匹配 /api/user/*
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (url.startsWith(prefix)) {
                    return true;
                }
            }

            // 3. 正则匹配
            if (url.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

}
