package com.campus.exam.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String DEFAULT_SECRET =
            "CampusExamPlatformJwtSecretKeyChangeInProductionMin256Bits!!";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            Environment environment) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret 长度至少 32 字节，请设置环境变量 APP_JWT_SECRET");
        }
        if (DEFAULT_SECRET.equals(secret) && !isLocalLike(environment)) {
            throw new IllegalStateException("非 local/test 环境必须通过 APP_JWT_SECRET 设置生产 JWT 密钥");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String createToken(String username, Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static boolean isLocalLike(Environment environment) {
        for (String profile : environment.getActiveProfiles()) {
            if ("local".equals(profile) || "test".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
