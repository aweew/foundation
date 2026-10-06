package com.awe.foundation.common.filter;

import com.awe.foundation.common.constant.Constants;
import com.awe.foundation.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.Objects;

/**
 * 统一请求链路标识过滤器
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private static final int MAX_TRACE_ID_LENGTH = 64;

    /**
     * 为请求生成或复用 traceId 并绑定到日志上下文
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
        String traceId = request.getHeader(Constants.TRACE_ID);
        if (Objects.isNull(traceId) || StringUtils.isBlank(traceId) || traceId.length() > MAX_TRACE_ID_LENGTH
                || !traceId.matches("[A-Za-z0-9._-]+")) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(Constants.TRACE_ID, traceId);
        response.setHeader(Constants.TRACE_ID, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(Constants.TRACE_ID);
        }
    }

}
