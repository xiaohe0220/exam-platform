package com.campus.exam.service;

import com.campus.exam.domain.Question;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.QuestionRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.QuestionRequest;
import com.campus.exam.web.dto.QuestionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<QuestionResponse> listMine(AuthenticatedUser user) {
        requireTeacher(user);
        return questionRepository.findByCreatorIdOrderByIdDesc(user.id()).stream()
                .map(q -> toResponse(q, true))
                .toList();
    }

    @Transactional
    public QuestionResponse create(AuthenticatedUser user, QuestionRequest req) {
        requireTeacher(user);
        Question q = new Question();
        apply(q, user.id(), req);
        questionRepository.save(q);
        return toResponse(q, true);
    }

    @Transactional
    public QuestionResponse update(AuthenticatedUser user, Long id, QuestionRequest req) {
        requireTeacher(user);
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!q.getCreatorId().equals(user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        apply(q, user.id(), req);
        return toResponse(q, true);
    }

    @Transactional
    public void delete(AuthenticatedUser user, Long id) {
        requireTeacher(user);
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!q.getCreatorId().equals(user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        questionRepository.delete(q);
    }

    private static void requireTeacher(AuthenticatedUser user) {
        String r = user.role();
        if (!UserRole.TEACHER.name().equals(r)
                && !UserRole.ADMIN.name().equals(r)
                && !UserRole.COLLEGE_ADMIN.name().equals(r)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要教师或管理员权限");
        }
    }

    private static void apply(Question q, Long creatorId, QuestionRequest req) {
        q.setCreatorId(creatorId);
        q.setType(req.type());
        q.setTitle(req.title());
        q.setContent(req.content());
        q.setOptionsJson(req.optionsJson());
        q.setCorrectAnswerJson(req.correctAnswerJson());
        q.setDifficulty(req.difficulty() != null ? req.difficulty() : 3);
        q.setChapter(req.chapter());
        q.setKnowledgePoint(req.knowledgePoint());
        q.setAnswerAnalysis(req.answerAnalysis());
    }

    public static QuestionResponse toResponse(Question q, boolean revealAnswer) {
        return new QuestionResponse(
                q.getId(),
                q.getType(),
                q.getTitle(),
                q.getContent(),
                q.getOptionsJson(),
                revealAnswer ? q.getCorrectAnswerJson() : null,
                revealAnswer,
                q.getDifficulty(),
                q.getChapter(),
                q.getKnowledgePoint(),
                revealAnswer ? q.getAnswerAnalysis() : null,
                q.getCreatedAt()
        );
    }

    /** 学生端展示乱序选项时不暴露标准答案。 */
    public static QuestionResponse toResponseShuffled(Question q, String displayOptionsJson) {
        return new QuestionResponse(
                q.getId(),
                q.getType(),
                q.getTitle(),
                q.getContent(),
                displayOptionsJson != null ? displayOptionsJson : q.getOptionsJson(),
                null,
                false,
                q.getDifficulty(),
                q.getChapter(),
                q.getKnowledgePoint(),
                null,
                q.getCreatedAt()
        );
    }
}
