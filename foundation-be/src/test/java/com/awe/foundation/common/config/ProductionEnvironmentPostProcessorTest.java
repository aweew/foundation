package com.awe.foundation.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 生产环境启动配置边界验证
 */
class ProductionEnvironmentPostProcessorTest {

    /**
     * 生产缺少必填配置时在连接依赖前终止
     */
    @Test
    void shouldRejectMissingProductionConfiguration() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(environment, null));
        assertTrue(exception.getMessage().contains("spring.datasource.url"));

    }

    /**
     * 生产拒绝开发密钥且不在异常中输出密钥
     */
    @Test
    void shouldRejectDevelopmentSecretWithoutExposingIt() {
        MockEnvironment environment = productionEnvironment();
        String developmentSecret = "local-foundation-jwt-key-0000000001";
        environment.setProperty("sa-token.jwt-secret-key", developmentSecret);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(environment, null));
        assertTrue(exception.getMessage().contains("sa-token.jwt-secret-key"));
        assertFalse(exception.getMessage().contains(developmentSecret));

    }

    /**
     * 生产拒绝弱密钥和缺失的 Redis 密码
     */
    @Test
    void shouldRejectShortSecretAndMissingRedisPassword() {
        MockEnvironment environment = productionEnvironment();
        environment.setProperty("foundation.storage.secret-key", "short-key");
        assertThrows(IllegalStateException.class,
                () -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(environment, null));
        MockEnvironment missingPasswordEnvironment = productionEnvironment();
        missingPasswordEnvironment.setProperty("spring.data.redis.password", "");
        assertThrows(IllegalStateException.class,
                () -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(missingPasswordEnvironment, null));

    }

    /**
     * 完整生产配置可以通过预检
     */
    @Test
    void shouldAcceptCompleteProductionConfiguration() {
        assertDoesNotThrow(() -> new ProductionEnvironmentPostProcessor()
                .postProcessEnvironment(productionEnvironment(), null));

    }

    /**
     * 开发环境无需通过生产预检
     */
    @Test
    void shouldSkipLocalConfiguration() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        assertDoesNotThrow(() -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(environment, null));

    }

    /**
     * 防止混合环境覆盖生产安全配置
     */
    @Test
    void shouldRejectMixedProductionProfiles() {
        MockEnvironment environment = productionEnvironment();
        environment.setActiveProfiles("prod", "local");
        assertThrows(IllegalStateException.class,
                () -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(environment, null));

    }

    /**
     * 签名与加密密钥必须隔离
     */
    @Test
    void shouldRejectReusedSecrets() {
        MockEnvironment environment = productionEnvironment();
        environment.setProperty("foundation.storage.secret-key", environment.getProperty("sa-token.jwt-secret-key"));
        assertThrows(IllegalStateException.class,
                () -> new ProductionEnvironmentPostProcessor().postProcessEnvironment(environment, null));

    }

    /**
     * 构造独立于外部服务的生产配置样本
     *
     * @return 生产配置样本
     */
    private MockEnvironment productionEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/foundation")
                .withProperty("spring.datasource.username", "foundation")
                .withProperty("spring.datasource.password", "production-database-password")
                .withProperty("spring.data.redis.host", "localhost")
                .withProperty("spring.data.redis.password", "production-redis-password")
                .withProperty("sa-token.jwt-secret-key", "production-jwt-secret-key-00000001")
                .withProperty("foundation.storage.secret-key", "production-storage-secret-0000001");
        environment.setActiveProfiles("prod");
        return environment;

    }

}
