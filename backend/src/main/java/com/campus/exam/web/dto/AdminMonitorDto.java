package com.campus.exam.web.dto;

import java.time.Instant;
import java.util.List;

public record AdminMonitorDto(
        Instant checkedAt,
        boolean apiOk,
        boolean databaseOk,
        boolean redisConfigured,
        boolean redisOk,
        long userTotal,
        long activeExamCount,
        long onlineExamSessions,
        long submittedToday,
        double cpuLoadPercent,
        long heapUsedMb,
        long heapMaxMb,
        List<NodeStatusDto> nodes,
        List<ActiveExamDto> activeExams
) {
    public record NodeStatusDto(
            String name,
            boolean ok,
            long ms,
            String message
    ) {
    }

    public record ActiveExamDto(
            Long examId,
            String title,
            String status,
            int progressPercent,
            long online,
            long submitted,
            long abnormalSwitchCount,
            boolean warn
    ) {
    }
}
