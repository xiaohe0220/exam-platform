package com.campus.exam.web;

import com.campus.exam.domain.ExamAttempt;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.StudentExamService;
import com.campus.exam.service.ExamRankingService;
import com.campus.exam.web.dto.AttemptResultDto;
import com.campus.exam.web.dto.ExamRankingRowDto;
import com.campus.exam.web.dto.ExamSummaryDto;
import com.campus.exam.web.dto.ObjectiveReviewItemDto;
import com.campus.exam.web.dto.PageResponse;
import com.campus.exam.web.dto.SecurityEventRequest;
import com.campus.exam.web.dto.StudentPaperDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamController {

    private final StudentExamService studentExamService;
    private final ExamRankingService examRankingService;

    public StudentExamController(StudentExamService studentExamService, ExamRankingService examRankingService) {
        this.studentExamService = studentExamService;
        this.examRankingService = examRankingService;
    }

    @GetMapping("/exams/{examId}/ranking")
    public List<ExamRankingRowDto> examRanking(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return examRankingService.rankingForStudent(user, examId);
    }

    @GetMapping("/exams/available")
    public PageResponse<ExamSummaryDto> available(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return studentExamService.listAvailablePage(user, page, size);
    }

    @PostMapping("/exams/{examId}/start")
    public ExamAttempt start(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return studentExamService.startOrResume(user, examId);
    }

    @GetMapping("/attempts/{attemptId}/paper")
    public StudentPaperDto paper(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId) {
        return studentExamService.loadPaper(user, attemptId);
    }

    @PutMapping("/attempts/{attemptId}/answers")
    public ExamAttempt saveAnswers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId,
            @RequestBody Map<String, Object> body) {
        return studentExamService.saveAnswers(user, attemptId, body);
    }

    @PostMapping("/attempts/{attemptId}/switch")
    public ExamAttempt reportSwitch(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId) {
        return studentExamService.reportSwitch(user, attemptId);
    }

    @PostMapping("/attempts/{attemptId}/security-events")
    public Map<String, String> securityEvent(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId,
            @Valid @RequestBody SecurityEventRequest req) {
        studentExamService.reportSecurityEvent(user, attemptId, req);
        return Map.of("message", "已记录");
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ExamAttempt submit(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId) {
        return studentExamService.submit(user, attemptId);
    }

    @GetMapping("/attempts/{attemptId}/result")
    public AttemptResultDto result(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId) {
        return studentExamService.getResult(user, attemptId);
    }

    @GetMapping("/attempts/{attemptId}/objective-review")
    public List<ObjectiveReviewItemDto> objectiveReview(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long attemptId) {
        return studentExamService.getObjectiveReview(user, attemptId);
    }
}
