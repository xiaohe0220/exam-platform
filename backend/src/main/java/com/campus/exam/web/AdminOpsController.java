package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.AcademicCatalogService;
import com.campus.exam.service.AdminExportService;
import com.campus.exam.service.AdminMonitorService;
import com.campus.exam.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','COLLEGE_ADMIN')")
public class AdminOpsController {

    private final AdminMonitorService adminMonitorService;
    private final AdminExportService adminExportService;
    private final AcademicCatalogService academicCatalogService;

    public AdminOpsController(
            AdminMonitorService adminMonitorService,
            AdminExportService adminExportService,
            AcademicCatalogService academicCatalogService) {
        this.adminMonitorService = adminMonitorService;
        this.adminExportService = adminExportService;
        this.academicCatalogService = academicCatalogService;
    }

    @GetMapping("/monitor/live")
    public AdminMonitorDto liveMonitor() {
        return adminMonitorService.live();
    }

    @GetMapping("/users/export")
    public ResponseEntity<ByteArrayResource> exportUsers() {
        return xlsx("users.xlsx", adminExportService.exportUsers());
    }

    @GetMapping("/audit-logs/export")
    public ResponseEntity<ByteArrayResource> exportAudit() {
        return xlsx("audit_logs.xlsx", adminExportService.exportAuditLogs());
    }

    @GetMapping("/analytics/export")
    public ResponseEntity<ByteArrayResource> exportAnalytics() {
        return xlsx("admin_analytics.xlsx", adminExportService.exportAnalytics());
    }

    @GetMapping("/backup/export")
    public ResponseEntity<ByteArrayResource> exportBackup() {
        return xlsx("exam_platform_backup_" + Instant.now().toString().replace(":", "-") + ".xlsx",
                adminExportService.exportBackupWorkbook());
    }

    @GetMapping("/backup/status")
    public Map<String, Object> backupStatus() {
        AdminMonitorDto m = adminMonitorService.live();
        return Map.of(
                "databaseOk", m.databaseOk(),
                "redisConfigured", m.redisConfigured(),
                "redisOk", m.redisOk(),
                "checkedAt", m.checkedAt(),
                "message", "可导出 Excel 工作簿备份核心业务数据");
    }

    @GetMapping("/academic/classes")
    public List<AcademicClassDto> classes() {
        return academicCatalogService.classes();
    }

    @PostMapping("/academic/classes")
    public AcademicClassDto createClass(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AcademicClassRequest req) {
        return academicCatalogService.createClass(user, req);
    }

    @PutMapping("/academic/classes/{id}")
    public AcademicClassDto updateClass(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody AcademicClassRequest req) {
        return academicCatalogService.updateClass(user, id, req);
    }

    @DeleteMapping("/academic/classes/{id}")
    public void deleteClass(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        academicCatalogService.deleteClass(user, id);
    }

    @GetMapping("/academic/courses")
    public List<CourseDto> courses() {
        return academicCatalogService.courses();
    }

    @PostMapping("/academic/courses")
    public CourseDto createCourse(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CourseRequest req) {
        return academicCatalogService.createCourse(user, req);
    }

    @PutMapping("/academic/courses/{id}")
    public CourseDto updateCourse(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest req) {
        return academicCatalogService.updateCourse(user, id, req);
    }

    @DeleteMapping("/academic/courses/{id}")
    public void deleteCourse(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        academicCatalogService.deleteCourse(user, id);
    }

    private static ResponseEntity<ByteArrayResource> xlsx(String filename, byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(bytes));
    }
}
