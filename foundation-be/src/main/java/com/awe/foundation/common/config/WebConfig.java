package com.awe.foundation.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.awe.foundation.common.constant.Constants;
import com.awe.foundation.common.config.properties.AppProperties;
import com.awe.foundation.common.convert.LocalDateConverter;
import com.awe.foundation.common.convert.LocalDateTimeConverter;
import com.awe.foundation.common.convert.LocalTimeConverter;
import com.awe.foundation.common.filter.RateLimitFilter;
import com.awe.foundation.common.filter.RepeatableFilter;
import com.awe.foundation.common.filter.TraceIdFilter;
import com.awe.foundation.common.filter.XssFilter;
import com.awe.foundation.common.interceptor.LogInterceptor;
import com.awe.foundation.common.interceptor.WebInvokeTimeInterceptor;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * web配置
 *
 * @author Awe
 * @date 2023/4/4 14:03
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AppProperties appProperties;

    @Resource
    private WebInvokeTimeInterceptor webInvokeTimeInterceptor;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 注册 MVC 拦截器
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/error", "/actuator/health", "/actuator/health/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**");
        // 日志拦截器
        registry.addInterceptor(new LogInterceptor()).addPathPatterns("/**");
        // 全局访问性能拦截
        registry.addInterceptor(webInvokeTimeInterceptor);
    }

    /**
     * 默认校验登录，匿名接口通过 SaIgnore 显式放行
     *
     * @return 登录与权限校验拦截器
     */
    @Bean
    public SaInterceptor authenticationInterceptor() {
        return new SaInterceptor(handler -> StpUtil.checkLogin());

    }

    /**
     * 注册日期时间转换器
     *
     * @param registry 格式化注册表
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LocalDateConverter());
        registry.addConverter(new LocalDateTimeConverter());
        registry.addConverter(new LocalTimeConverter());
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(appProperties.getCors().getAllowedOrigins());
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "satoken", "X-Trace-Id", Constants.TRACE_ID));
        config.setExposedHeaders(List.of(Constants.TRACE_ID));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }

    /**
     * 注册可重复读取请求体的过滤器
     */
    @Bean
    public FilterRegistrationBean<RepeatableFilter> repeatableFilter() {
        FilterRegistrationBean<RepeatableFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RepeatableFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    /**
     * 注册统一 traceId 过滤器
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(-100);
        return registration;
    }

    /**
     * 注册请求限流过滤器
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        AppProperties.RateLimit rateLimit = appProperties.getRateLimit();
        registration.setFilter(new RateLimitFilter(rateLimit.isEnabled(), rateLimit.getRequestsPerWindow(),
                rateLimit.getWindowSeconds(), rateLimit.getExcludePaths(), stringRedisTemplate));
        registration.addUrlPatterns("/*");
        registration.setOrder(-90);
        return registration;
    }

    /**
     * 注册 XSS 请求过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addInitParameter("excludes", appProperties.getSecurity().getXssExcludes());
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

}
