package com.campus.exam.web;

import com.campus.exam.domain.Exam;
import com.campus.exam.domain.ExamAttempt;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.ExamService;
import com.campus.exam.service.TeachersExamMapper;
import com.campus.exam.web.dto.ExamCreateRequest;
import com.campus.exam.web.dto.ExamSummaryDto;
import com.campus.exam.web.dto.GradeRequest;
import com.campus.exam.web.dto.RandomComposeRequest;
import com.campus.exam.service.ExamStatsService;
import com.campus.exam.service.ExamReportService;
import com.campus.exam.service.StudentExamService;
import com.campus.exam.service.TeacherAttemptDetailService;
import com.campus.exam.service.TeacherGradeService;
import com.campus.exam.service.ExamRankingService;
import com.campus.exam.web.dto.ExamMetaPatchRequest;
import com.campus.exam.web.dto.ExamRankingRowDto;
import com.campus.exam.web.dto.ExamStatsDto;
import com.campus.exam.web.dto.ExamQuestionStatsDto;
import com.campus.exam.web.dto.PageResponse;
import com.campus.exam.web.dto.TeacherAttemptDetailDto;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/exams")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','COLLEGE_ADMIN')")
public class TeacherExamController {

    private final ExamService examService;
    private final ExamAttemptRepository examAttemptRepository;
    private final TeacherGradeService teacherGradeService;
    private final ExamStatsService examStatsService;
    private final TeacherAttemptDetailService teacherAttemptDetailService;
    private final StudentExamService studentExamService;
    private final ExamRankingService examRankingService;
    private final ExamReportService examReportService;

    public TeacherExamController(
            ExamService examService,
            ExamAttemptRepository examAttemptRepository,
            TeacherGradeService teacherGradeService,
            ExamStatsService examStatsService,
            TeacherAttemptDetailService teacherAttemptDetailService,
            StudentExamService studentExamService,
            ExamRankingService examRankingService,
            ExamReportService examReportService) {
        this.examService = examService;
        this.examAttemptRepository = examAttemptRepository;
        this.teacherGradeService = teacherGradeService;
        this.examStatsService = examStatsService;
        this.teacherAttemptDetailService = teacherAttemptDetailService;
        this.studentExamService = studentExamService;
        this.examRankingService = examRankingService;
        this.examReportService = examReportService;
    }

    @GetMapping("/{examId}/ranking")
    public List<ExamRankingRowDto> ranking(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return examRankingService.rankingForTeacher(user, examId);
    }

    @PatchMapping("/{examId}/meta")
    public ExamSummaryDto patchMeta(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId,
            @RequestBody ExamMetaPatchRequest req) {
        Exam e = examService.patchMeta(user, examId, req);
        return TeachersExamMapper.toSummary(e);
    }

    @PostMapping("/{examId}/attempts/{attemptId}/grade")
    public com.campus.exam.domain.ExamAttempt grade(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId,
            @PathVariable Long attemptId,
            @Valid @RequestBody GradeRequest req) {
        return teacherGradeService.gradeSubjective(user, examId, attemptId, req.questionId(), req.score(), req.comment());
    }

    @GetMapping
    public PageResponse<ExamSummaryDto> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int sz = Math.min(Math.max(size, 1), 100);
        Pageable p = PageRequest.of(Math.max(page, 0), sz);
        return PageResponse.fromPage(examService.listMine(user, p), TeachersExamMapper::toSummary);
    }

    @PostMapping
    public ExamSummaryDto createFixed(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ExamCreateRequest req) {
        Exam e = examService.createFixed(user, req);
        return TeachersExamMapper.toSummary(e);
    }

    @PostMapping("/compose-random")
    public ExamSummaryDto composeRandom(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RandomComposeRequest req) {
        Exam e = examService.composeRandom(user, req);
        return TeachersExamMapper.toSummary(e);
    }

    @PostMapping("/{examId}/publish")
    public ExamSummaryDto publish(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        Exam e = examService.publish(user, examId);
        return TeachersExamMapper.toSummary(e);
    }

    @PostMapping("/{examId}/close")
    public ExamSummaryDto close(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        Exam e = examService.close(user, examId);
        return TeachersExamMapper.toSummary(e);
    }

    @GetMapping("/{examId}/attempts")
    public List<ExamAttempt> attempts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        examService.requireOwnedExam(user, examId);
        return examAttemptRepository.findByExamId(examId);
    }

    @GetMapping("/{examId}/stats")
    public ExamStatsDto stats(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return examStatsService.stats(user, examId);
    }

    @GetMapping("/{examId}/question-stats")
    public List<ExamQuestionStatsDto> questionStats(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return examReportService.questionStats(user, examId);
    }

    @GetMapping("/{examId}/attempts/export")
    public ResponseEntity<ByteArrayResource> exportAttempts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return xlsx("exam_" + examId + "_attempts.xlsx", examReportService.exportAttempts(user, examId));
    }

    @GetMapping("/{examId}/question-stats/export")
    public ResponseEntity<ByteArrayResource> exportQuestionStats(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId) {
        return xlsx("exam_" + examId + "_question_stats.xlsx", examReportService.exportQuestionStats(user, examId));
    }

    @GetMapping("/{examId}/attempts/{attemptId}/detail")
    public TeacherAttemptDetailDto attemptDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId,
            @PathVariable Long attemptId) {
        return teacherAttemptDetailService.detail(user, examId, attemptId);
    }

    /** 延长考试结束可进入时间 */
    @PostMapping("/{examId}/extend-end")
    public ExamSummaryDto extendEnd(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId,
            @RequestBody(required = false) Map<String, Integer> body) {
        int add = body != null && body.get("addMinutes") != null ? body.get("addMinutes") : 0;
        Exam e = examService.extendEndTime(user, examId, add);
        return TeachersExamMapper.toSummary(e);
    }

    /** 对未交卷学生强制收卷并计分 */
    @PostMapping("/{examId}/attempts/{attemptId}/force-submit")
    public ExamAttempt forceSubmit(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long examId,
            @PathVariable Long attemptId) {
        return studentExamService.teacherForceSubmit(user, examId, attemptId);
    }

    private static ResponseEntity<ByteArrayResource> xlsx(String filename, byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(bytes));
    }
}
