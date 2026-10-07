package com.awe.foundation.common.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 分布式幂等键服务
 */
@Service
public class IdempotencyService {

    @Resource
    private RedisService redisService;

    /**
     * 尝试登记请求幂等键
     *
     * @param requestKey 客户端请求键
     * @param timeout    幂等有效期
     * @return 首次请求返回true
     */
    public boolean tryAcquire(String requestKey, Duration timeout) {
        return redisService.trySet("idempotency:" + requestKey, timeout);
    }

}
