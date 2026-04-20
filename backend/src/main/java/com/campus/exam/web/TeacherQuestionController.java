package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.QuestionService;
import com.campus.exam.web.dto.QuestionRequest;
import com.campus.exam.web.dto.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/questions")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','COLLEGE_ADMIN')")
public class TeacherQuestionController {

    private final QuestionService questionService;

    public TeacherQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return questionService.listMine(user);
    }

    @PostMapping
    public QuestionResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody QuestionRequest req) {
        return questionService.create(user, req);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest req) {
        return questionService.update(user, id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        questionService.delete(user, id);
    }
}
