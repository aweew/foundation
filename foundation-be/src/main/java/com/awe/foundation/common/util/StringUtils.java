package com.awe.foundation.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.util.AntPathMatcher;

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

    private static final AntPathMatcher URL_PATH_MATCHER = new AntPathMatcher();

    /**
     * 判断字符串是否忽略大小写前缀匹配
     *
     * @param value  目标字符串
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
     * 判断 URL 是否匹配排除列表，支持 Ant 路径规则中的 *、** 和 ?
     *
     * @param url      请求路径
     * @param patterns 排除路径规则
     * @return 是否匹配排除路径
     */
    public static boolean matches(String url, List<String> patterns) {
        if (isBlank(url) || CollUtil.isEmpty(patterns)) {
            return false;
        }

        for (String urlPattern : patterns) {
            if (isBlank(urlPattern)) {
                continue;
            }

            if (URL_PATH_MATCHER.match(urlPattern, url)) {
                return true;
            }
        }
        return false;
    }

}
