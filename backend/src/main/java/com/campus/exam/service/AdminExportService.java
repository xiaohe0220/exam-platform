package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import com.campus.exam.web.dto.AdminAnalyticsDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class AdminExportService {

    private final UserAccountRepository userAccountRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AuditLogRepository auditLogRepository;
    private final AcademicClassRepository academicClassRepository;
    private final CourseRepository courseRepository;
    private final AdminAnalyticsService adminAnalyticsService;

    public AdminExportService(
            UserAccountRepository userAccountRepository,
            QuestionRepository questionRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            AuditLogRepository auditLogRepository,
            AcademicClassRepository academicClassRepository,
            CourseRepository courseRepository,
            AdminAnalyticsService adminAnalyticsService) {
        this.userAccountRepository = userAccountRepository;
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.auditLogRepository = auditLogRepository;
        this.academicClassRepository = academicClassRepository;
        this.courseRepository = courseRepository;
        this.adminAnalyticsService = adminAnalyticsService;
    }

    public byte[] exportUsers() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            writeUsers(wb.createSheet("users"));
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("导出用户失败", e);
        }
    }

    public byte[] exportAuditLogs() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            writeAudit(wb.createSheet("audit_logs"));
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("导出审计失败", e);
        }
    }

    public byte[] exportAnalytics() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet overview = wb.createSheet("overview");
            ExamReportService.header(overview, new String[]{"metric", "value"});
            AdminAnalyticsDto data = adminAnalyticsService.overview();
            int r = 1;
            r = metric(overview, r, "studentCount", data.studentCount());
            r = metric(overview, r, "teacherCount", data.teacherCount());
            r = metric(overview, r, "publishedExamCount", data.publishedExamCount());
            r = metric(overview, r, "submittedAttemptCount", data.submittedAttemptCount());
            r = metric(overview, r, "participationRate", data.participationRate());
            r = metric(overview, r, "avgScoreAllAttempts", data.avgScoreAllAttempts());
            metric(overview, r, "avgQuestionDifficulty", data.avgQuestionDifficulty());
            ExamReportService.autosize(overview, 2);

            Sheet effects = wb.createSheet("exam_effects");
            ExamReportService.header(effects, new String[]{"examId", "title", "attemptCount", "avgScore"});
            int i = 1;
            for (AdminAnalyticsDto.ExamEffectRow row : data.examEffects()) {
                Row er = effects.createRow(i++);
                ExamReportService.cell(er, 0, row.examId());
                ExamReportService.cell(er, 1, row.title());
                ExamReportService.cell(er, 2, row.attemptCount());
                ExamReportService.cell(er, 3, row.avgScore());
            }
            ExamReportService.autosize(effects, 4);
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("导出分析失败", e);
        }
    }

    public byte[] exportBackupWorkbook() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            writeUsers(wb.createSheet("users"));
            writeQuestions(wb.createSheet("questions"));
            writeExams(wb.createSheet("exams"));
            writeAttempts(wb.createSheet("attempts"));
            writeAudit(wb.createSheet("audit_logs"));
            writeClasses(wb.createSheet("academic_classes"));
            writeCourses(wb.createSheet("courses"));
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("导出备份失败", e);
        }
    }

    private int metric(Sheet sh, int rowIdx, String name, Object value) {
        Row row = sh.createRow(rowIdx++);
        ExamReportService.cell(row, 0, name);
        ExamReportService.cell(row, 1, value);
        return rowIdx;
    }

    private void writeUsers(Sheet sh) {
        String[] h = {"id", "username", "displayName", "role", "className", "college", "email", "phone", "enabled", "createdAt"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (UserAccount u : userAccountRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, u.getId());
            ExamReportService.cell(row, 1, u.getUsername());
            ExamReportService.cell(row, 2, u.getDisplayName());
            ExamReportService.cell(row, 3, u.getRole());
            ExamReportService.cell(row, 4, u.getClassName());
            ExamReportService.cell(row, 5, u.getCollege());
            ExamReportService.cell(row, 6, u.getEmail());
            ExamReportService.cell(row, 7, u.getPhone());
            ExamReportService.cell(row, 8, !Boolean.FALSE.equals(u.getEnabled()));
            ExamReportService.cell(row, 9, u.getCreatedAt());
        }
        ExamReportService.autosize(sh, h.length);
    }

    private void writeQuestions(Sheet sh) {
        String[] h = {"id", "creatorId", "type", "title", "content", "optionsJson", "correctAnswerJson", "difficulty", "chapter", "knowledgePoint", "answerAnalysis", "createdAt"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (Question q : questionRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, q.getId());
            ExamReportService.cell(row, 1, q.getCreatorId());
            ExamReportService.cell(row, 2, q.getType());
            ExamReportService.cell(row, 3, q.getTitle());
            ExamReportService.cell(row, 4, q.getContent());
            ExamReportService.cell(row, 5, q.getOptionsJson());
            ExamReportService.cell(row, 6, q.getCorrectAnswerJson());
            ExamReportService.cell(row, 7, q.getDifficulty());
            ExamReportService.cell(row, 8, q.getChapter());
            ExamReportService.cell(row, 9, q.getKnowledgePoint());
            ExamReportService.cell(row, 10, q.getAnswerAnalysis());
            ExamReportService.cell(row, 11, q.getCreatedAt());
        }
        ExamReportService.autosize(sh, h.length);
    }

    private void writeExams(Sheet sh) {
        String[] h = {"id", "creatorId", "title", "description", "startAt", "endAt", "durationMinutes", "maxRetakes", "switchLimit", "fullscreenRequired", "status", "targetClasses", "rankingVisible", "createdAt"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (Exam e : examRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, e.getId());
            ExamReportService.cell(row, 1, e.getCreatorId());
            ExamReportService.cell(row, 2, e.getTitle());
            ExamReportService.cell(row, 3, e.getDescription());
            ExamReportService.cell(row, 4, e.getStartAt());
            ExamReportService.cell(row, 5, e.getEndAt());
            ExamReportService.cell(row, 6, e.getDurationMinutes());
            ExamReportService.cell(row, 7, e.getMaxRetakes());
            ExamReportService.cell(row, 8, e.getSwitchLimit());
            ExamReportService.cell(row, 9, e.getFullscreenRequired());
            ExamReportService.cell(row, 10, e.getStatus());
            ExamReportService.cell(row, 11, e.getTargetClasses());
            ExamReportService.cell(row, 12, e.getRankingVisible());
            ExamReportService.cell(row, 13, e.getCreatedAt());
        }
        ExamReportService.autosize(sh, h.length);
    }

    private void writeAttempts(Sheet sh) {
        String[] h = {"id", "examId", "userId", "status", "startedAt", "submittedAt", "switchCount", "objectiveScore", "subjectiveScore", "totalScore"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (ExamAttempt a : examAttemptRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, a.getId());
            ExamReportService.cell(row, 1, a.getExamId());
            ExamReportService.cell(row, 2, a.getUserId());
            ExamReportService.cell(row, 3, a.getStatus());
            ExamReportService.cell(row, 4, a.getStartedAt());
            ExamReportService.cell(row, 5, a.getSubmittedAt());
            ExamReportService.cell(row, 6, a.getSwitchCount());
            ExamReportService.cell(row, 7, a.getObjectiveScore());
            ExamReportService.cell(row, 8, a.getSubjectiveScore());
            ExamReportService.cell(row, 9, a.getTotalScore());
        }
        ExamReportService.autosize(sh, h.length);
    }

    private void writeAudit(Sheet sh) {
        String[] h = {"id", "userId", "action", "detail", "createdAt"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (AuditLog a : auditLogRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, a.getId());
            ExamReportService.cell(row, 1, a.getUserId());
            ExamReportService.cell(row, 2, a.getAction());
            ExamReportService.cell(row, 3, a.getDetail());
            ExamReportService.cell(row, 4, a.getCreatedAt());
        }
        ExamReportService.autosize(sh, h.length);
    }

    private void writeClasses(Sheet sh) {
        String[] h = {"id", "name", "college", "major", "grade", "enabled", "createdAt"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (AcademicClass c : academicClassRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, c.getId());
            ExamReportService.cell(row, 1, c.getName());
            ExamReportService.cell(row, 2, c.getCollege());
            ExamReportService.cell(row, 3, c.getMajor());
            ExamReportService.cell(row, 4, c.getGrade());
            ExamReportService.cell(row, 5, !Boolean.FALSE.equals(c.getEnabled()));
            ExamReportService.cell(row, 6, c.getCreatedAt());
        }
        ExamReportService.autosize(sh, h.length);
    }

    private void writeCourses(Sheet sh) {
        String[] h = {"id", "code", "name", "college", "teacherName", "enabled", "createdAt"};
        ExamReportService.header(sh, h);
        int r = 1;
        for (Course c : courseRepository.findAll()) {
            Row row = sh.createRow(r++);
            ExamReportService.cell(row, 0, c.getId());
            ExamReportService.cell(row, 1, c.getCode());
            ExamReportService.cell(row, 2, c.getName());
            ExamReportService.cell(row, 3, c.getCollege());
            ExamReportService.cell(row, 4, c.getTeacherName());
            ExamReportService.cell(row, 5, !Boolean.FALSE.equals(c.getEnabled()));
            ExamReportService.cell(row, 6, c.getCreatedAt());
        }
        ExamReportService.autosize(sh, h.length);
    }
}
