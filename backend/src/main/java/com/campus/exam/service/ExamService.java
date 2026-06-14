package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.ExamAttemptRepository;
import com.campus.exam.repository.ExamQuestionRepository;
import com.campus.exam.repository.ExamRepository;
import com.campus.exam.repository.QuestionRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.ExamCreateRequest;
import com.campus.exam.web.dto.ExamMetaPatchRequest;
import com.campus.exam.web.dto.RandomComposeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;
    private final AuditService auditService;
    private final InAppNotificationService inAppNotificationService;

    public ExamService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamAttemptRepository examAttemptRepository,
            QuestionRepository questionRepository,
            AuditService auditService,
            InAppNotificationService inAppNotificationService) {
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
        this.auditService = auditService;
        this.inAppNotificationService = inAppNotificationService;
    }

    public List<Exam> listMine(AuthenticatedUser user) {
        requireStaff(user);
        return examRepository.findByCreatorIdOrderByIdDesc(user.id());
    }

    public Page<Exam> listMine(AuthenticatedUser user, Pageable pageable) {
        requireStaff(user);
        return examRepository.findByCreatorIdOrderByIdDesc(user.id(), pageable);
    }

    /** 校验考试归属（创建者或系统管理员）。 */
    public Exam requireOwnedExam(AuthenticatedUser user, Long examId) {
        return getOwnedExam(user, examId);
    }

    @Transactional
    public Exam createFixed(AuthenticatedUser user, ExamCreateRequest req) {
        requireStaff(user);
        Exam e = new Exam();
        e.setCreatorId(user.id());
        applyExamMeta(e, req);
        e.setStatus(ExamStatus.DRAFT);
        examRepository.save(e);
        if (req.questions() != null) {
            int idx = 1;
            for (ExamCreateRequest.ExamQuestionItem it : req.questions()) {
                ExamQuestion eq = new ExamQuestion();
                eq.setExamId(e.getId());
                eq.setQuestionId(it.questionId());
                eq.setOrderIndex(it.orderIndex() != null ? it.orderIndex() : idx++);
                eq.setScore(it.score() != null ? it.score() : BigDecimal.TEN);
                examQuestionRepository.save(eq);
            }
        }
        auditService.log(user.id(), "EXAM_CREATE", "id=" + e.getId());
        return examRepository.findById(e.getId()).orElseThrow();
    }

    @Transactional
    public Exam composeRandom(AuthenticatedUser user, RandomComposeRequest req) {
        requireStaff(user);
        List<Question> bank = questionRepository.findByCreatorIdOrderByIdDesc(user.id());
        if (req.chapterKeyword() != null && !req.chapterKeyword().isBlank()) {
            String kw = req.chapterKeyword();
            bank = bank.stream()
                    .filter(q -> q.getChapter() != null && q.getChapter().contains(kw))
                    .toList();
        }
        if (req.difficulty() != null) {
            int d = req.difficulty();
            bank = bank.stream().filter(q -> Objects.equals(q.getDifficulty(), d)).toList();
        }

        Map<QuestionType, List<Question>> byType = bank.stream()
                .collect(Collectors.groupingBy(Question::getType));
        for (Map.Entry<QuestionType, Integer> en : req.countByType().entrySet()) {
            List<Question> list = byType.get(en.getKey());
            if (list == null || list.size() < en.getValue()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "题库中题型 " + en.getKey() + " 数量不足");
            }
        }

        Exam e = new Exam();
        e.setCreatorId(user.id());
        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setStartAt(req.startAt());
        e.setEndAt(req.endAt());
        e.setDurationMinutes(req.durationMinutes());
        e.setMaxRetakes(req.maxRetakes() != null ? req.maxRetakes() : 1);
        e.setSwitchLimit(req.switchLimit() != null ? req.switchLimit() : 3);
        e.setFullscreenRequired(req.fullscreenRequired() != null ? req.fullscreenRequired() : true);
        e.setTargetClasses(req.targetClasses());
        e.setRankingVisible(req.rankingVisible() != null ? req.rankingVisible() : true);
        e.setStatus(ExamStatus.DRAFT);
        examRepository.save(e);

        int order = 1;
        BigDecimal per = req.scorePerQuestion();
        for (Map.Entry<QuestionType, Integer> en : req.countByType().entrySet()) {
            List<Question> list = new ArrayList<>(byType.get(en.getKey()));
            Collections.shuffle(list);
            for (int i = 0; i < en.getValue(); i++) {
                Question q = list.get(i);
                ExamQuestion eq = new ExamQuestion();
                eq.setExamId(e.getId());
                eq.setQuestionId(q.getId());
                eq.setOrderIndex(order++);
                eq.setScore(per != null ? per : BigDecimal.TEN);
                examQuestionRepository.save(eq);
            }
        }
        auditService.log(user.id(), "EXAM_RANDOM_COMPOSE", "id=" + e.getId());
        return e;
    }

    @Transactional
    public Exam publish(AuthenticatedUser user, Long examId) {
        requireStaff(user);
        Exam e = getOwnedExam(user, examId);
        e.setStatus(ExamStatus.PUBLISHED);
        auditService.log(user.id(), "EXAM_PUBLISH", "id=" + examId);
        examRepository.save(e);
        inAppNotificationService.notifyExamPublished(e);
        return e;
    }

    @Transactional
    public Exam patchMeta(AuthenticatedUser user, Long examId, ExamMetaPatchRequest req) {
        requireStaff(user);
        Exam e = getOwnedExam(user, examId);
        if (req.rankingVisible() != null) {
            e.setRankingVisible(req.rankingVisible());
        }
        if (req.maxRetakes() != null) {
            int m = req.maxRetakes();
            if (m < 1 || m > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "重考次数需在 1–20 之间");
            }
            e.setMaxRetakes(m);
        }
        if (req.durationMinutes() != null) {
            int d = req.durationMinutes();
            if (d < 5 || d > 600) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "考试时长需在 5–600 分钟之间");
            }
            e.setDurationMinutes(d);
        }
        auditService.log(user.id(), "EXAM_META_PATCH", "id=" + examId);
        return examRepository.save(e);
    }

    @Transactional
    public Exam close(AuthenticatedUser user, Long examId) {
        requireStaff(user);
        Exam e = getOwnedExam(user, examId);
        e.setStatus(ExamStatus.CLOSED);
        return examRepository.save(e);
    }

    @Transactional
    public void delete(AuthenticatedUser user, Long examId) {
        requireStaff(user);
        Exam e = getOwnedExam(user, examId);
        if (examAttemptRepository.countByExamId(examId) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已有学生作答记录，不能删除，可先关闭试卷");
        }
        examQuestionRepository.deleteByExamId(examId);
        examRepository.delete(e);
        auditService.log(user.id(), "EXAM_DELETE", "id=" + examId);
    }

    /** 延长考试可进入截止时间（在原有 endAt 基础上顺延，若为空则从当前时间起算） */
    @Transactional
    public Exam extendEndTime(AuthenticatedUser user, Long examId, int addMinutes) {
        requireStaff(user);
        if (addMinutes <= 0 || addMinutes > 1440) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "延长分钟数需在 1–1440 之间");
        }
        Exam e = getOwnedExam(user, examId);
        Instant base = e.getEndAt() != null ? e.getEndAt() : Instant.now();
        e.setEndAt(base.plus(addMinutes, ChronoUnit.MINUTES));
        auditService.log(user.id(), "EXAM_EXTEND", "examId=" + examId + " addMin=" + addMinutes);
        return examRepository.save(e);
    }

    private Exam getOwnedExam(AuthenticatedUser user, Long examId) {
        Exam e = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (UserRole.ADMIN.name().equals(user.role()) || UserRole.COLLEGE_ADMIN.name().equals(user.role())) {
            return e;
        }
        if (!e.getCreatorId().equals(user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return e;
    }

    private static void applyExamMeta(Exam e, ExamCreateRequest req) {
        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setStartAt(req.startAt());
        e.setEndAt(req.endAt());
        e.setDurationMinutes(req.durationMinutes());
        e.setMaxRetakes(req.maxRetakes() != null ? req.maxRetakes() : 1);
        e.setSwitchLimit(req.switchLimit() != null ? req.switchLimit() : 3);
        e.setFullscreenRequired(req.fullscreenRequired() != null ? req.fullscreenRequired() : true);
        e.setTargetClasses(req.targetClasses());
        e.setRankingVisible(req.rankingVisible() != null ? req.rankingVisible() : true);
    }

    private static void requireStaff(AuthenticatedUser user) {
        String r = user.role();
        if (!UserRole.TEACHER.name().equals(r)
                && !UserRole.ADMIN.name().equals(r)
                && !UserRole.COLLEGE_ADMIN.name().equals(r)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要教师或管理员权限");
        }
    }

    public static boolean isVisibleToStudent(Exam e, UserAccount student, Instant now) {
        if (e.getStatus() != ExamStatus.PUBLISHED) {
            return false;
        }
        if (e.getStartAt() != null && now.isBefore(e.getStartAt())) {
            return false;
        }
        if (e.getEndAt() != null && now.isAfter(e.getEndAt())) {
            return false;
        }
        String targets = e.getTargetClasses();
        if (targets == null || targets.isBlank()) {
            return true;
        }
        Set<String> set = Arrays.stream(targets.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        String cn = student.getClassName();
        return cn != null && set.contains(cn);
    }
}
