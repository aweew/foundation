package com.awe.foundation.common.filter;

import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 基于客户端地址和请求路径的固定窗口限流过滤器
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int requestsPerWindow;
    private final long windowMillis;
    private final List<String> excludePaths;
    private final StringRedisTemplate redisTemplate;

    /**
     * 初始化限流参数
     *
     * @param enabled           是否启用限流
     * @param requestsPerWindow 时间窗口内允许的请求数
     * @param windowSeconds     时间窗口秒数
     * @param excludePaths      排除路径
     */
    public RateLimitFilter(boolean enabled, int requestsPerWindow, long windowSeconds, String excludePaths,
                           StringRedisTemplate redisTemplate) {
        this.enabled = enabled;
        this.requestsPerWindow = requestsPerWindow;
        this.windowMillis = Duration.ofSeconds(windowSeconds).toMillis();
        this.excludePaths = parseExcludePaths(excludePaths);
        this.redisTemplate = redisTemplate;
    }

    /**
     * 按客户端地址和请求路径执行限流
     *
     * @param request     请求对象
     * @param response    响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      IO 异常
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
        Long requestCount = redisTemplate.opsForValue().increment("foundation:rate-limit:" + key);
        if (requestCount != null && requestCount == 1L) {
            redisTemplate.expire("foundation:rate-limit:" + key, Duration.ofMillis(windowMillis));
        }
        if (requestCount != null && requestCount > requestsPerWindow) {
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

}
