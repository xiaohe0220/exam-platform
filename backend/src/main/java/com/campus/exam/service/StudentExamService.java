package com.campus.exam.service;

import com.campus.exam.domain.*;
import com.campus.exam.repository.*;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.util.AnswerJsonUtil;
import com.campus.exam.util.OptionShuffleUtil;
import com.campus.exam.web.dto.AttemptResultDto;
import com.campus.exam.web.dto.ExamSummaryDto;
import com.campus.exam.web.dto.ObjectiveReviewItemDto;
import com.campus.exam.web.dto.PageResponse;
import com.campus.exam.web.dto.QuestionResponse;
import com.campus.exam.web.dto.SecurityEventRequest;
import com.campus.exam.web.dto.StudentPaperDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentExamService {

    private final UserAccountRepository userAccountRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final GradingService gradingService;
    private final AuditService auditService;
    private final SubmitLockService submitLockService;
    private final ExamService examService;
    private final InAppNotificationService inAppNotificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudentExamService(
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            GradingService gradingService,
            AuditService auditService,
            SubmitLockService submitLockService,
            ExamService examService,
            InAppNotificationService inAppNotificationService) {
        this.userAccountRepository = userAccountRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.gradingService = gradingService;
        this.auditService = auditService;
        this.submitLockService = submitLockService;
        this.examService = examService;
        this.inAppNotificationService = inAppNotificationService;
    }

    /**
     * 先按「已发布 + 时间窗口」缩小候选集，再按班级可见性过滤并分页。
     */
    public PageResponse<ExamSummaryDto> listAvailablePage(AuthenticatedUser user, int page, int size) {
        requireStudent(user);
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        UserAccount stu = userAccountRepository.findById(user.id()).orElseThrow();
        Instant now = Instant.now();
        List<Exam> candidates = examRepository.findPublishedInWindow(ExamStatus.PUBLISHED, now);
        List<Exam> visible = candidates.stream()
                .filter(e -> ExamService.isVisibleToStudent(e, stu, now))
                .sorted(Comparator.comparing(Exam::getStartAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        long total = visible.size();
        int from = Math.min(page * size, visible.size());
        int to = Math.min(from + size, visible.size());
        List<ExamSummaryDto> slice = visible.subList(from, to).stream()
                .map(TeachersExamMapper::toSummary)
                .toList();
        return PageResponse.of(slice, page, size, total);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public ExamAttempt startOrResume(AuthenticatedUser user, Long examId) {
        requireStudent(user);
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserAccount stu = userAccountRepository.findById(user.id()).orElseThrow();
        Instant now = Instant.now();
        if (!ExamService.isVisibleToStudent(exam, stu, now)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "考试不可访问");
        }

        long taken = examAttemptRepository.countByExamIdAndUserId(examId, user.id());
        var inProgress = examAttemptRepository.findByExamIdAndUserIdAndStatus(examId, user.id(), AttemptStatus.IN_PROGRESS);
        if (inProgress.isPresent()) {
            ExamAttempt current = inProgress.get();
            if (isAttemptExpired(exam, current, now)) {
                finalizeSubmit(user, current, true, false);
            } else {
                return current;
            }
        }
        if (taken >= exam.getMaxRetakes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已达到最大考试次数");
        }

        ExamAttempt a = new ExamAttempt();
        a.setExamId(examId);
        a.setUserId(user.id());
        a.setStatus(AttemptStatus.IN_PROGRESS);
        a.setStartedAt(now);
        a.setAnswersJson("{}");
        a.setSwitchCount(0);
        examAttemptRepository.save(a);
        auditService.log(user.id(), "EXAM_START", "examId=" + examId + " attemptId=" + a.getId());
        return a;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public ExamAttempt saveAnswers(AuthenticatedUser user, Long attemptId, Map<String, Object> body) {
        requireStudent(user);
        ExamAttempt a = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!a.getUserId().equals(user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (a.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "已交卷");
        }
        Exam exam = examRepository.findById(a.getExamId()).orElseThrow();
        if (isAttemptExpired(exam, a, Instant.now())) {
            finalizeSubmit(user, a, true, false);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "考试已超时，系统已自动交卷");
        }
        Map<String, Object> answers = new LinkedHashMap<>(body);
        Object marked = answers.remove("markedQuestionIds");
        try {
            a.setAnswersJson(objectMapper.writeValueAsString(answers));
            if (marked != null) {
                a.setFlagsJson(objectMapper.writeValueAsString(marked));
            }
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "答案格式错误");
        }
        return examAttemptRepository.save(a);
    }

    /**
     * 教师对未交卷答卷强制收卷并计分（需拥有该考试）。
     */
    @Transactional
    public ExamAttempt teacherForceSubmit(AuthenticatedUser teacher, Long examId, Long attemptId) {
        examService.requireOwnedExam(teacher, examId);
        ExamAttempt a = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!a.getExamId().equals(examId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "答卷不属于该考试");
        }
        if (a.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "答卷已结束");
        }
        return finalizeSubmit(teacher, a, false, true);
    }

    public List<ObjectiveReviewItemDto> getObjectiveReview(AuthenticatedUser user, Long attemptId) {
        requireStudent(user);
        ExamAttempt a = loadOwnedAttempt(user, attemptId);
        if (a.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "尚未交卷");
        }
        Exam exam = examRepository.findById(a.getExamId()).orElseThrow();
        Map<Long, Question> qmap = loadQuestionMap(exam.getId());
        Map<Long, String> display = AnswerJsonUtil.toMap(a.getAnswersJson());
        Map<Long, String> graded = OptionShuffleUtil.toOriginalAnswers(display, a.getShuffleJson(), qmap);
        List<ObjectiveReviewItemDto> out = new ArrayList<>();
        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId())) {
            Question q = qmap.get(eq.getQuestionId());
            if (q == null || q.getType() == QuestionType.SHORT_ANSWER) {
                continue;
            }
            String orig = graded.get(q.getId());
            boolean ok = gradingService.isObjectiveCorrect(q, orig);
            BigDecimal earned = ok ? eq.getScore() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            out.add(new ObjectiveReviewItemDto(
                    q.getId(),
                    q.getTitle(),
                    q.getType().name(),
                    display.getOrDefault(q.getId(), ""),
                    q.getCorrectAnswerJson(),
                    ok,
                    eq.getScore(),
                    earned));
        }
        return out;
    }

    @Transactional
    public ExamAttempt reportSwitch(AuthenticatedUser user, Long attemptId) {
        requireStudent(user);
        ExamAttempt a = loadOwnedAttempt(user, attemptId);
        Exam exam = examRepository.findById(a.getExamId()).orElseThrow();
        if (a.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "考试已结束");
        }
        if (isAttemptExpired(exam, a, Instant.now())) {
            return finalizeSubmit(user, a, true, false);
        }
        a.setSwitchCount(a.getSwitchCount() + 1);
        if (a.getSwitchCount() >= exam.getSwitchLimit()) {
            return finalizeSubmit(user, a, true, false);
        }
        return examAttemptRepository.save(a);
    }

    @Transactional
    public void reportSecurityEvent(AuthenticatedUser user, Long attemptId, SecurityEventRequest req) {
        requireStudent(user);
        ExamAttempt a = loadOwnedAttempt(user, attemptId);
        String type = req.eventType() != null ? req.eventType().trim() : "UNKNOWN";
        String detail = req.detail() != null ? req.detail().trim() : "";
        if (type.length() > 80) {
            type = type.substring(0, 80);
        }
        if (detail.length() > 500) {
            detail = detail.substring(0, 500);
        }
        auditService.log(user.id(), "EXAM_SECURITY_EVENT",
                "attempt=" + a.getId() + " exam=" + a.getExamId() + " type=" + type + " detail=" + detail);
    }

    @Transactional
    public ExamAttempt submit(AuthenticatedUser user, Long attemptId) {
        requireStudent(user);
        ExamAttempt a = loadOwnedAttempt(user, attemptId);
        Exam exam = examRepository.findById(a.getExamId()).orElseThrow();
        boolean expired = isAttemptExpired(exam, a, Instant.now());
        return finalizeSubmit(user, a, expired, false);
    }

    private ExamAttempt finalizeSubmit(AuthenticatedUser user, ExamAttempt a0, boolean auto, boolean teacherForce) {
        synchronized (("submit-" + a0.getId()).intern()) {
            ExamAttempt a = examAttemptRepository.findById(a0.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (a.getStatus() != AttemptStatus.IN_PROGRESS) {
                return a;
            }
            if (!submitLockService.tryAcquireSubmitLock(a.getId())) {
                ExamAttempt again = examAttemptRepository.findById(a.getId()).orElseThrow();
                if (again.getStatus() != AttemptStatus.IN_PROGRESS) {
                    return again;
                }
                throw new ResponseStatusException(HttpStatus.CONFLICT, "正在交卷，请稍候再试");
            }

            Exam exam = examRepository.findById(a.getExamId()).orElseThrow();
            Map<Long, String> displayMap = AnswerJsonUtil.toMap(a.getAnswersJson());
            Map<Long, Question> qmap = loadQuestionMap(exam.getId());
            Map<Long, String> gradeMap = OptionShuffleUtil.toOriginalAnswers(displayMap, a.getShuffleJson(), qmap);

            BigDecimal objective = gradingService.gradeObjective(exam.getId(), gradeMap, qmap);
            BigDecimal subjective = gradingService.sumSubjective(a.getSubjectiveScoresJson());
            BigDecimal total = objective.add(subjective);

            a.setObjectiveScore(objective);
            a.setSubjectiveScore(subjective);
            a.setTotalScore(total);
            a.setSubmittedAt(Instant.now());
            a.setStatus(auto ? AttemptStatus.AUTO_SUBMITTED : AttemptStatus.SUBMITTED);
            String auditAction = auto ? "EXAM_AUTO_SUBMIT" : (teacherForce ? "EXAM_FORCE_SUBMIT" : "EXAM_SUBMIT");
            auditService.log(user.id(), auditAction,
                    "attempt=" + a.getId() + " exam=" + exam.getId());
            examAttemptRepository.save(a);
            inAppNotificationService.notifyGradePublished(a.getUserId(), exam.getTitle(), exam.getId());
            return a;
        }
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public StudentPaperDto loadPaper(AuthenticatedUser user, Long attemptId) {
        requireStudent(user);
        ExamAttempt att = loadOwnedAttempt(user, attemptId);
        if (att.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "考试已结束");
        }
        Exam exam = examRepository.findById(att.getExamId()).orElseThrow();
        if (isAttemptExpired(exam, att, Instant.now())) {
            finalizeSubmit(user, att, true, false);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "考试已超时，系统已自动交卷");
        }

        ObjectNode shuffleRoot;
        try {
            if (att.getShuffleJson() == null || att.getShuffleJson().isBlank()) {
                shuffleRoot = objectMapper.createObjectNode();
            } else {
                shuffleRoot = (ObjectNode) objectMapper.readTree(att.getShuffleJson());
            }
        } catch (Exception e) {
            shuffleRoot = objectMapper.createObjectNode();
        }

        boolean needPersistShuffle = att.getShuffleJson() == null || att.getShuffleJson().isBlank();

        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId())) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElseThrow();
            if (!needPersistShuffle) {
                continue;
            }
            if ((q.getType() == QuestionType.SINGLE_CHOICE || q.getType() == QuestionType.MULTIPLE_CHOICE)
                    && q.getOptionsJson() != null && !q.getOptionsJson().isBlank()) {
                try {
                    List<String> opts = objectMapper.readValue(q.getOptionsJson(), new TypeReference<>() {
                    });
                    if (opts.size() >= 2) {
                        List<Integer> perm = OptionShuffleUtil.randomPermutation(opts.size());
                        ArrayNode arr = objectMapper.createArrayNode();
                        for (Integer i : perm) {
                            arr.add(i);
                        }
                        shuffleRoot.set(String.valueOf(q.getId()), arr);
                    }
                } catch (Exception ignored) {
                    /* skip */
                }
            }
        }

        if (needPersistShuffle) {
            try {
                att.setShuffleJson(objectMapper.writeValueAsString(shuffleRoot));
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "乱序数据保存失败");
            }
            examAttemptRepository.save(att);
        }

        Map<String, List<Integer>> shuffleMap = parseShuffleMap(att.getShuffleJson());
        List<QuestionResponse> qs = new ArrayList<>();
        for (ExamQuestion eq : examQuestionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId())) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElseThrow();
            List<Integer> perm = shuffleMap.get(String.valueOf(q.getId()));
            if (perm != null && !perm.isEmpty()
                    && (q.getType() == QuestionType.SINGLE_CHOICE || q.getType() == QuestionType.MULTIPLE_CHOICE)
                    && q.getOptionsJson() != null) {
                try {
                    String shuffled = OptionShuffleUtil.shuffleOptionsJson(q.getOptionsJson(), perm);
                    qs.add(QuestionService.toResponseShuffled(q, shuffled));
                } catch (Exception e) {
                    qs.add(QuestionService.toResponse(q, false));
                }
            } else {
                qs.add(QuestionService.toResponse(q, false));
            }
        }

        ExamSummaryDto sum = toSummary(exam);
        List<Long> marked = parseMarkedIds(att.getFlagsJson());
        return new StudentPaperDto(sum, att.getId(), att.getStartedAt(), att.getSwitchCount(), marked, qs);
    }

    private List<Long> parseMarkedIds(String flagsJson) {
        if (flagsJson == null || flagsJson.isBlank()) {
            return List.of();
        }
        try {
            List<?> arr = objectMapper.readValue(flagsJson, new TypeReference<List<Object>>() {
            });
            List<Long> out = new ArrayList<>();
            for (Object o : arr) {
                if (o instanceof Number n) {
                    out.add(n.longValue());
                } else if (o != null) {
                    out.add(Long.parseLong(o.toString()));
                }
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, List<Integer>> parseShuffleMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static boolean isAttemptExpired(Exam exam, ExamAttempt attempt, Instant now) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return false;
        }
        if (exam.getEndAt() != null && !now.isBefore(exam.getEndAt())) {
            return true;
        }
        Integer durationMinutes = exam.getDurationMinutes();
        if (durationMinutes != null && durationMinutes > 0 && attempt.getStartedAt() != null) {
            Instant deadline = attempt.getStartedAt().plus(durationMinutes, ChronoUnit.MINUTES);
            return !now.isBefore(deadline);
        }
        return false;
    }

    public AttemptResultDto getResult(AuthenticatedUser user, Long attemptId) {
        requireStudent(user);
        ExamAttempt a = loadOwnedAttempt(user, attemptId);
        if (a.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "尚未交卷");
        }
        return new AttemptResultDto(
                a.getId(),
                a.getExamId(),
                a.getStatus(),
                a.getSubmittedAt(),
                a.getObjectiveScore(),
                a.getSubjectiveScore(),
                a.getTotalScore(),
                a.getSubjectiveScoresJson());
    }

    Map<Long, Question> loadQuestionMap(Long examId) {
        List<ExamQuestion> eqs = examQuestionRepository.findByExamIdOrderByOrderIndexAsc(examId);
        Map<Long, Question> map = new HashMap<>();
        for (ExamQuestion eq : eqs) {
            questionRepository.findById(eq.getQuestionId()).ifPresent(q -> map.put(q.getId(), q));
        }
        return map;
    }

    private ExamAttempt loadOwnedAttempt(AuthenticatedUser user, Long attemptId) {
        ExamAttempt a = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!a.getUserId().equals(user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return a;
    }

    private static void requireStudent(AuthenticatedUser user) {
        if (!UserRole.STUDENT.name().equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要学生权限");
        }
    }

    private static ExamSummaryDto toSummary(Exam e) {
        return new ExamSummaryDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                e.getStartAt(),
                e.getEndAt(),
                e.getDurationMinutes(),
                e.getMaxRetakes(),
                e.getSwitchLimit(),
                e.getFullscreenRequired(),
                e.getTargetClasses(),
                e.getRankingVisible());
    }
}
