package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.StudentMessageService;
import com.campus.exam.web.dto.StudentMessageDto;
import com.campus.exam.web.dto.StudentMessageReplyRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师与管理员查看学生留言并回复。
 */
@RestController
@RequestMapping("/api/staff/feedback")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','COLLEGE_ADMIN')")
public class StaffFeedbackController {

    private final StudentMessageService studentMessageService;

    public StaffFeedbackController(StudentMessageService studentMessageService) {
        this.studentMessageService = studentMessageService;
    }

    @GetMapping
    public List<StudentMessageDto> list() {
        return studentMessageService.listAllForStaff();
    }

    @PostMapping("/{id}/reply")
    public StudentMessageDto reply(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody StudentMessageReplyRequest req) {
        return studentMessageService.reply(user, id, req.reply());
    }
}
