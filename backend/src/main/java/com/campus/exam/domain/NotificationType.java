package com.campus.exam.domain;

public enum NotificationType {
    /** 新考试发布 */
    EXAM_PUBLISHED,
    /** 成绩已生成 / 已交卷 */
    GRADE_PUBLISHED,
    /** 开考提醒（预留，可由定时任务触发） */
    EXAM_REMINDER
}
