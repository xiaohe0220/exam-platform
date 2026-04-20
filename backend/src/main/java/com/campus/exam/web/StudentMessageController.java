package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.StudentMessageService;
import com.campus.exam.web.dto.StudentMessageCreateRequest;
import com.campus.exam.web.dto.StudentMessageDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/messages")
@PreAuthorize("hasRole('STUDENT')")
public class StudentMessageController {

    private final StudentMessageService studentMessageService;

    public StudentMessageController(StudentMessageService studentMessageService) {
        this.studentMessageService = studentMessageService;
    }

    @GetMapping
    public List<StudentMessageDto> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return studentMessageService.listMine(user);
    }

    @PostMapping
    public StudentMessageDto create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StudentMessageCreateRequest req) {
        return studentMessageService.create(user, req);
    }
}
