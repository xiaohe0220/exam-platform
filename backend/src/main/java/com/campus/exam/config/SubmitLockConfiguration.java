package com.campus.exam.config;

import com.campus.exam.service.NoopSubmitLockService;
import com.campus.exam.service.RedisSubmitLockService;
import com.campus.exam.service.SubmitLockService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 先注册 Redis 实现（有 StringRedisTemplate 时），否则注册空实现，避免 @Service + 条件注解顺序问题。
 */
@Configuration
public class SubmitLockConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public SubmitLockService redisSubmitLockService(StringRedisTemplate redis) {
        return new RedisSubmitLockService(redis);
    }

    @Bean
    @ConditionalOnMissingBean(SubmitLockService.class)
    public SubmitLockService noopSubmitLockService() {
        return new NoopSubmitLockService();
    }
}
