package com.awe.foundation.common.filter;

import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于客户端地址和请求路径的固定窗口限流过滤器
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int requestsPerWindow;
    private final long windowMillis;
    private final List<String> excludePaths;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    /**
     * 初始化限流参数
     *
     * @param enabled 是否启用限流
     * @param requestsPerWindow 时间窗口内允许的请求数
     * @param windowSeconds 时间窗口秒数
     * @param excludePaths 排除路径
     */
    public RateLimitFilter(boolean enabled, int requestsPerWindow, long windowSeconds, String excludePaths) {
        this.enabled = enabled;
        this.requestsPerWindow = requestsPerWindow;
        this.windowMillis = Duration.ofSeconds(windowSeconds).toMillis();
        this.excludePaths = parseExcludePaths(excludePaths);
    }

    /**
     * 按客户端地址和请求路径执行限流
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestPath = request.getServletPath();
        if (!enabled || isExcluded(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + ":" + requestPath;
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(System.currentTimeMillis()));
        if (!counter.tryAcquire(System.currentTimeMillis(), requestsPerWindow, windowMillis)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtils.toJsonString(Result.failure(ErrorCodeEnum.TOO_MANY_REQUESTS)));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断请求路径是否排除限流
     *
     * @param requestUri 请求路径
     * @return 是否排除
     */
    private boolean isExcluded(String requestUri) {
        return excludePaths.stream().anyMatch(requestUri::startsWith);
    }

    /**
     * 解析逗号分隔的排除路径
     *
     * @param value 配置值
     * @return 排除路径列表
     */
    private List<String> parseExcludePaths(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .toList();
    }

    private static class WindowCounter {

        private long windowStart;
        private int requestCount;

        private WindowCounter(long windowStart) {
            this.windowStart = windowStart;
        }

        /**
         * 尝试占用一个时间窗口内的请求额度
         *
         * @param now 当前时间
         * @param limit 请求上限
         * @param windowMillis 时间窗口毫秒数
         * @return 是否获取成功
         */
        private synchronized boolean tryAcquire(long now, int limit, long windowMillis) {
            if (now - windowStart >= windowMillis) {
                windowStart = now;
                requestCount = 0;
            }
            if (requestCount >= limit) {
                return false;
            }
            requestCount++;
            return true;
        }
    }

}
