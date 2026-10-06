package com.awe.foundation.common.filter;

import com.awe.foundation.common.util.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 防止XSS攻击的过滤器
 *
 * @author ydd
 */
public class XssFilter implements Filter {

    /**
     * 排除链接
     */
    public List<String> excludes = new ArrayList<>();

    /**
     * 初始化 XSS 排除路径
     *
     * @param filterConfig 过滤器配置
     * @throws ServletException Servlet 处理异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotBlank(tempExcludes)) {
            String[] url = tempExcludes.split(StringUtils.SEPARATOR);
            for (int i = 0; Objects.nonNull(url) && i < url.length; i++) {
                if (StringUtils.isNotBlank(url[i])) {
                    excludes.add(url[i].trim());
                }
            }
        }
    }

    /**
     * 过滤请求参数和请求体
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param chain 过滤器链
     * @throws IOException IO 异常
     * @throws ServletException Servlet 处理异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    /**
     * 判断请求是否跳过 XSS 处理
     *
     * @param request 请求对象
     * @param response 响应对象
     * @return 是否跳过
     */
    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (Objects.isNull(method) || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        return StringUtils.matches(url, excludes);
    }

    /**
     * 销毁过滤器
     */
    @Override
    public void destroy() {

    }

}
