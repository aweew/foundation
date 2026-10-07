package com.awe.foundation.common.config;

import com.awe.foundation.common.handler.BigDecimalSerializer;
import com.awe.foundation.common.handler.BigNumberSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;

/**
 * Jackson 配置
 *
 * @author Awe
 * @since 2025/12/9 14:46
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 全局配置序列化返回 JSON 处理
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            builder.serializersByType(Map.of(
                    Long.class, BigNumberSerializer.INSTANCE,
                    Long.TYPE, BigNumberSerializer.INSTANCE,
                    BigInteger.class, BigNumberSerializer.INSTANCE,
                    BigDecimal.class, BigDecimalSerializer.INSTANCE,
                    LocalDateTime.class, new LocalDateTimeSerializer(formatter)
            ));
            builder.deserializersByType(Map.of(
                    LocalDateTime.class, new LocalDateTimeDeserializer(formatter)
            ));
            builder.timeZone(TimeZone.getDefault());
            log.info("初始化 jackson 配置");
        };
    }

}
