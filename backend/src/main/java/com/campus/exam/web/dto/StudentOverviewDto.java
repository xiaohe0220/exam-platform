package com.campus.exam.web.dto;

import java.math.BigDecimal;

public record StudentOverviewDto(
        BigDecimal avgScore,
        long examFinishedCount,
        Integer classRank,
        Integer schoolRank,
        int classPeerCount,
        int schoolPeerCount
) {
}
