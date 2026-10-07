package com.awe.foundation.common.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 分布式锁服务
 */
@Service
public class DistributedLockService {

    @Resource
    private RedisService redisService;

    /**
     * 尝试获取锁并返回持有者标识
     *
     * @param key     业务锁键
     * @param timeout 锁过期时间
     * @return 持有者标识，获取失败返回null
     */
    public String tryLock(String key, Duration timeout) {
        String token = UUID.randomUUID().toString();
        return redisService.tryLock(key, token, timeout) ? token : null;
    }

    /**
     * 释放锁
     *
     * @param key   业务锁键
     * @param token 持有者标识
     * @return 是否释放成功
     */
    public boolean unlock(String key, String token) {
        return redisService.unlock(key, token);
    }

}
