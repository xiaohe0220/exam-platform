package com.campus.exam.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String msg = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    @ExceptionHandler({DataAccessException.class, PersistenceException.class})
    public ResponseEntity<Map<String, Object>> handleDataAccess(Exception ex) {
        log.error("数据库访问失败", ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "数据库错误，请确认已执行迁移且表结构为最新（可检查 ddl-auto 或重启后端）");
        if (isDevFriendlyProfile()) {
            Throwable root = ex.getCause() != null ? ex.getCause() : ex;
            body.put("detail", root.getClass().getSimpleName() + ": " + root.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (msg.isEmpty()) {
            msg = "请求参数无效";
        }
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        log.error("未处理异常", ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "服务器内部错误");
        if (isDevFriendlyProfile()) {
            body.put("detail", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /** 本地 / 开发时在响应里附带简要原因，便于排查（勿在生产环境启用 dev/local/test profile 对外暴露） */
    private boolean isDevFriendlyProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equals("local") || p.equals("dev") || p.equals("test"));
    }
}
