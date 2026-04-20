package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.TeacherDashboardService;
import com.campus.exam.web.dto.TeacherDashboardDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/dashboard")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN','COLLEGE_ADMIN')")
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    public TeacherDashboardController(TeacherDashboardService teacherDashboardService) {
        this.teacherDashboardService = teacherDashboardService;
    }

    @GetMapping("/summary")
    public TeacherDashboardDto summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return teacherDashboardService.summary(user);
    }
}
