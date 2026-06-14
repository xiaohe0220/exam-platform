package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.QuestionImportService;
import com.campus.exam.service.QuestionService;
import com.campus.exam.web.dto.QuestionRequest;
import com.campus.exam.web.dto.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/questions")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','COLLEGE_ADMIN')")
public class TeacherQuestionController {

    private final QuestionService questionService;
    private final QuestionImportService questionImportService;

    public TeacherQuestionController(QuestionService questionService, QuestionImportService questionImportService) {
        this.questionService = questionService;
        this.questionImportService = questionImportService;
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

    @GetMapping("/import-template")
    public ResponseEntity<ByteArrayResource> importTemplate() {
        return xlsx("question_import_template.xlsx", questionImportService.template());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public QuestionImportService.ImportResult importQuestions(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("file") MultipartFile file) {
        return questionImportService.importExcel(user, file);
    }

    private static ResponseEntity<ByteArrayResource> xlsx(String filename, byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(bytes));
    }
}
