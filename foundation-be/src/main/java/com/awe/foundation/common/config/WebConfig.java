package com.awe.foundation.common.config;

import com.awe.foundation.common.convert.LocalDateConverter;
import com.awe.foundation.common.convert.LocalDateTimeConverter;
import com.awe.foundation.common.convert.LocalTimeConverter;
import com.awe.foundation.common.filter.RepeatableFilter;
import com.awe.foundation.common.filter.XssFilter;
import com.awe.foundation.common.interceptor.LogInterceptor;
import com.awe.foundation.common.interceptor.WebInvokeTimeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${app.security.xss-excludes:}")
    private String xssExcludes;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志拦截器
        registry.addInterceptor(new LogInterceptor()).addPathPatterns("/**");
        // 全局访问性能拦截
        registry.addInterceptor(new WebInvokeTimeInterceptor());
    }

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
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Trace-Id"));
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
     * 注册 XSS 请求过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addInitParameter("excludes", xssExcludes);
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

}
