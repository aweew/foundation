package com.awe.foundation.common.service;

import com.awe.foundation.common.config.properties.AppProperties;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Redis基础服务
 */
@Service
public class RedisService {

    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AppProperties appProperties;

    /**
     * 写入带过期时间的字符串
     *
     * @param key     业务键
     * @param value   值
     * @param timeout 过期时间
     */
    public void set(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(buildKey(key), value, timeout);
    }

    /**
     * 读取字符串
     *
     * @param key 业务键
     * @return 值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(buildKey(key));
    }

    /**
     * 删除业务键
     *
     * @param key 业务键
     * @return 是否删除
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(buildKey(key)));
    }

    /**
     * 原子获取幂等键
     *
     * @param key     业务键
     * @param timeout 有效期
     * @return 首次写入返回true
     */
    public boolean trySet(String key, Duration timeout) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(buildKey(key), "1", timeout));
    }

    /**
     * 原子递增计数并设置首次过期时间
     *
     * @param key     业务键
     * @param timeout 窗口时长
     * @return 当前计数
     */
    public long increment(String key, Duration timeout) {
        String redisKey = buildKey(key);
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (Objects.equals(count, 1L)) {
            stringRedisTemplate.expire(redisKey, timeout);
        }
        return Objects.requireNonNullElse(count, 0L);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param key     锁键
     * @param token   锁持有者标识
     * @param timeout 锁过期时间
     * @return 是否获取成功
     */
    public boolean tryLock(String key, String token, Duration timeout) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(buildKey(key), token, timeout));
    }

    /**
     * 仅释放当前持有者的分布式锁
     *
     * @param key   锁键
     * @param token 锁持有者标识
     * @return 是否释放成功
     */
    public boolean unlock(String key, String token) {
        Long result = stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(buildKey(key)), token);
        return Objects.equals(result, 1L);
    }

    /**
     * 添加统一业务键前缀
     *
     * @param key 原始键
     * @return Redis键
     */
    private String buildKey(String key) {
        return appProperties.getRedis().getKeyPrefix() + key;
    }

}
