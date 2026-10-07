package com.awe.foundation.common.config;

import com.awe.foundation.common.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

import java.nio.charset.StandardCharsets;

/**
 * 生产环境配置预检，在数据库与 Redis 初始化前阻止无效配置
 */
public class ProductionEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * 校验生产依赖连接信息和认证密钥
     *
     * @param environment 已加载配置文件的运行环境
     * @param application Spring 应用
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }
        if (environment.acceptsProfiles(Profiles.of("local", "test"))) {
            throw new IllegalStateException("prod 不能与 local 或 test 同时启用");
        }

        String[] requiredProperties = {"spring.datasource.url", "spring.datasource.username",
                "spring.datasource.password", "spring.data.redis.host", "spring.data.redis.password"};
        for (String propertyName : requiredProperties) {
            String propertyValue = environment.getProperty(propertyName);
            if (StringUtils.isBlank(propertyValue) || propertyValue.startsWith("foundation-local")) {
                throw new IllegalStateException("生产环境必须配置 " + propertyName);
            }

        }

        String[] secretProperties = {"sa-token.jwt-secret-key", "foundation.storage.secret-key"};
        for (String propertyName : secretProperties) {
            String secretValue = environment.getProperty(propertyName);
            if (StringUtils.isBlank(secretValue) || secretValue.startsWith("local-")
                    || secretValue.startsWith("test-") || secretValue.startsWith("change-me")
                    || secretValue.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException("生产环境必须为 " + propertyName + " 配置至少 32 字节的独立密钥");
            }

        }
        if (environment.getProperty("sa-token.jwt-secret-key")
                .equals(environment.getProperty("foundation.storage.secret-key"))) {
            throw new IllegalStateException("JWT 签名和存储凭据加密必须使用不同密钥");
        }

    }

    /**
     * 在 Spring Boot 加载环境配置后执行预检
     *
     * @return 环境处理器执行顺序
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;

    }

}
