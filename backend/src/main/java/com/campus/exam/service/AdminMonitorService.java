package com.campus.exam.service;

import com.campus.exam.domain.AttemptStatus;
import com.campus.exam.domain.Exam;
import com.campus.exam.domain.ExamAttempt;
import com.campus.exam.domain.ExamStatus;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.web.dto.AdminMonitorDto;
import com.campus.exam.web.dto.AdminMonitorDto.ActiveExamDto;
import com.campus.exam.web.dto.AdminMonitorDto.NodeStatusDto;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMonitorService {

    private final DataSource dataSource;
    private final ObjectProvider<StringRedisTemplate> redisProvider;
    private final UserAccountRepository userAccountRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final boolean redisEnabled;

    public AdminMonitorService(
            DataSource dataSource,
            ObjectProvider<StringRedisTemplate> redisProvider,
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            @Value("${app.redis.enabled:false}") boolean redisEnabled) {
        this.dataSource = dataSource;
        this.redisProvider = redisProvider;
        this.userAccountRepository = userAccountRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.redisEnabled = redisEnabled;
    }

    public AdminMonitorDto live() {
        Instant now = Instant.now();
        Probe db = probeDatabase();
        Probe redis = probeRedis();
        List<ActiveExamDto> active = activeExams(now);
        long online = active.stream().mapToLong(ActiveExamDto::online).sum();
        long submittedToday = submittedToday();
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long maxMb = rt.maxMemory() / 1024 / 1024;

        List<NodeStatusDto> nodes = new ArrayList<>();
        nodes.add(new NodeStatusDto("API", true, 0, "OK"));
        nodes.add(new NodeStatusDto("DB", db.ok(), db.ms(), db.message()));
        nodes.add(new NodeStatusDto("Redis", !redisEnabled || redis.ok(), redis.ms(), redis.message()));
        nodes.add(new NodeStatusDto("JVM", usedMb < maxMb * 0.9, 0, usedMb + "MB / " + maxMb + "MB"));

        return new AdminMonitorDto(
                now,
                true,
                db.ok(),
                redisEnabled,
                !redisEnabled || redis.ok(),
                userAccountRepository.count(),
                active.size(),
                online,
                submittedToday,
                cpuLoadPercent(),
                usedMb,
                maxMb,
                nodes,
                active);
    }

    private Probe probeDatabase() {
        long start = System.nanoTime();
        try (Connection c = dataSource.getConnection()) {
            boolean ok = c.isValid(1);
            return new Probe(ok, elapsedMs(start), ok ? "OK" : "连接不可用");
        } catch (Exception e) {
            return new Probe(false, elapsedMs(start), e.getClass().getSimpleName());
        }
    }

    private Probe probeRedis() {
        if (!redisEnabled) {
            return new Probe(true, 0, "未启用");
        }
        long start = System.nanoTime();
        try {
            StringRedisTemplate redis = redisProvider.getIfAvailable();
            if (redis == null) {
                return new Probe(false, elapsedMs(start), "未装配");
            }
            try (RedisConnection conn = redis.getConnectionFactory().getConnection()) {
                String pong = conn.ping();
                return new Probe(pong != null, elapsedMs(start), pong != null ? pong : "无响应");
            }
        } catch (Exception e) {
            return new Probe(false, elapsedMs(start), e.getClass().getSimpleName());
        }
    }

    private List<ActiveExamDto> activeExams(Instant now) {
        return examRepository.findByStatus(ExamStatus.PUBLISHED).stream()
                .filter(e -> e.getEndAt() == null || e.getEndAt().isAfter(now))
                .map(e -> {
                    List<ExamAttempt> attempts = examAttemptRepository.findByExamId(e.getId());
                    long online = attempts.stream().filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS).count();
                    long submitted = attempts.stream().filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS).count();
                    int limit = e.getSwitchLimit() != null ? e.getSwitchLimit() : 3;
                    long abnormal = attempts.stream()
                            .filter(a -> a.getSwitchCount() != null && a.getSwitchCount() >= Math.max(1, limit - 1))
                            .count();
                    boolean warn = abnormal > 0 || online > 300;
                    return new ActiveExamDto(
                            e.getId(),
                            e.getTitle(),
                            e.getStatus().name(),
                            progress(e, now),
                            online,
                            submitted,
                            abnormal,
                            warn);
                })
                .toList();
    }

    private long submittedToday() {
        Instant start = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return examAttemptRepository.findAll().stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .filter(a -> a.getSubmittedAt() != null && !a.getSubmittedAt().isBefore(start))
                .count();
    }

    private static int progress(Exam e, Instant now) {
        if (e.getStartAt() == null || e.getEndAt() == null) {
            return 0;
        }
        long total = Math.max(1, Duration.between(e.getStartAt(), e.getEndAt()).toSeconds());
        long done = Math.max(0, Duration.between(e.getStartAt(), now).toSeconds());
        return (int) Math.max(0, Math.min(100, Math.round(done * 100.0 / total)));
    }

    private static double cpuLoadPercent() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean os) {
            double load = os.getCpuLoad();
            if (load >= 0) {
                return Math.round(load * 1000.0) / 10.0;
            }
        }
        double avg = bean.getSystemLoadAverage();
        return avg >= 0 ? Math.round(avg * 10.0) / 10.0 : 0;
    }

    private static long elapsedMs(long start) {
        return Math.max(0, Math.round((System.nanoTime() - start) / 1_000_000.0));
    }

    private record Probe(boolean ok, long ms, String message) {
    }
}
