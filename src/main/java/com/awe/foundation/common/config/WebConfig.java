package com.awe.foundation.common.config;

import cn.hutool.core.collection.CollUtil;
import com.awe.foundation.common.convert.LocalDateConverter;
import com.awe.foundation.common.convert.LocalDateTimeConverter;
import com.awe.foundation.common.convert.LocalTimeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * web配置
 *
 * @author Awe
 * @date 2023/4/4 14:03
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> defaultPath = CollUtil.newArrayList("/**");

    private final List<String> excludePath = Arrays.asList(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v2/**",
            "/v3/**",
            "/**/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/test/**",
            "/**/login",
            "/csrf"
    );

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LocalDateConverter());
        registry.addConverter(new LocalDateTimeConverter());
        registry.addConverter(new LocalTimeConverter());
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configSource);
    }

}
