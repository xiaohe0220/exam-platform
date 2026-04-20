package com.campus.exam.service;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public class RedisSubmitLockService implements SubmitLockService {

    private static final String PREFIX = "exam:submit:";
    private final StringRedisTemplate redis;

    public RedisSubmitLockService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean tryAcquireSubmitLock(Long attemptId) {
        Boolean ok = redis.opsForValue().setIfAbsent(PREFIX + attemptId, "1", Duration.ofHours(48));
        return Boolean.TRUE.equals(ok);
    }
}
