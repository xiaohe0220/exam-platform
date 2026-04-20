package com.campus.exam.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败累计与短时锁定，降低暴力破解风险（内存实现，多实例需换 Redis 等）。
 */
@Service
public class LoginProtectionService {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MS = 15 * 60_000L;

    private final ConcurrentHashMap<String, Integer> failures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockUntil = new ConcurrentHashMap<>();

    public void checkAllowed(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = username.trim().toLowerCase();
        Long until = lockUntil.get(key);
        if (until != null && System.currentTimeMillis() < until) {
            long sec = Math.max(1, (until - System.currentTimeMillis()) / 1000);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "登录尝试过多，请约 " + sec + " 秒后再试");
        }
        if (until != null && System.currentTimeMillis() >= until) {
            lockUntil.remove(key);
            failures.remove(key);
        }
    }

    public void recordFailure(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = username.trim().toLowerCase();
        int n = failures.merge(key, 1, Integer::sum);
        if (n >= MAX_FAILURES) {
            lockUntil.put(key, System.currentTimeMillis() + LOCK_MS);
            failures.remove(key);
        }
    }

    public void recordSuccess(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = username.trim().toLowerCase();
        failures.remove(key);
        lockUntil.remove(key);
    }
}
