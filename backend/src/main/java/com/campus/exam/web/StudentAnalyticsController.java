package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.StudentAnalyticsService;
import com.campus.exam.web.dto.StudentAttemptHistoryItemDto;
import com.campus.exam.web.dto.StudentOverviewDto;
import com.campus.exam.web.dto.WrongQuestionItemDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/analytics")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAnalyticsController {

    private final StudentAnalyticsService studentAnalyticsService;

    public StudentAnalyticsController(StudentAnalyticsService studentAnalyticsService) {
        this.studentAnalyticsService = studentAnalyticsService;
    }

    @GetMapping("/overview")
    public StudentOverviewDto overview(@AuthenticationPrincipal AuthenticatedUser user) {
        return studentAnalyticsService.overview(user);
    }

    @GetMapping("/wrong-questions")
    public List<WrongQuestionItemDto> wrongQuestions(@AuthenticationPrincipal AuthenticatedUser user) {
        return studentAnalyticsService.wrongQuestions(user);
    }

    @GetMapping("/attempt-history")
    public List<StudentAttemptHistoryItemDto> history(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "20") int limit) {
        return studentAnalyticsService.attemptHistory(user, limit);
    }
}
