package com.awe.foundation.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用通用配置
 */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 跨域配置
     */
    private Cors cors = new Cors();

    /**
     * Web 安全配置
     */
    private Security security = new Security();

    /**
     * 请求限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Cors {

        /**
         * 允许跨域的来源
         */
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000"));

    }

    @Data
    public static class Security {

        /**
         * XSS 过滤排除路径
         */
        private String xssExcludes;

    }

    @Data
    public static class RateLimit {

        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 时间窗口内允许的请求数
         */
        private int requestsPerWindow = 120;

        /**
         * 限流时间窗口，单位为秒
         */
        private long windowSeconds = 60;

        /**
         * 限流排除路径
         */
        private String excludePaths;

    }

}
