package com.campus.exam.web;

import com.campus.exam.domain.AuditLog;
import com.campus.exam.repository.AuditLogRepository;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.AdminStatsService;
import com.campus.exam.service.UserImportService;
import com.campus.exam.web.dto.AdminOverviewDto;
import com.campus.exam.web.dto.AdminUserListItemDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','COLLEGE_ADMIN')")
public class AdminController {

    private final UserAccountRepository userAccountRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserImportService userImportService;
    private final AdminStatsService adminStatsService;

    public AdminController(
            UserAccountRepository userAccountRepository,
            AuditLogRepository auditLogRepository,
            UserImportService userImportService,
            AdminStatsService adminStatsService) {
        this.userAccountRepository = userAccountRepository;
        this.auditLogRepository = auditLogRepository;
        this.userImportService = userImportService;
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/stats/overview")
    public AdminOverviewDto statsOverview() {
        return adminStatsService.overview();
    }

    @GetMapping("/users")
    public List<AdminUserListItemDto> users() {
        return userAccountRepository.findAll().stream()
                .map(AdminUserListItemDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> auditLogs(@RequestParam(defaultValue = "100") int limit) {
        return auditLogRepository.findAll(PageRequest.of(0, Math.min(limit, 500))).getContent();
    }

    @GetMapping("/users/import-template")
    public ResponseEntity<ByteArrayResource> importTemplate() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("users");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("username");
            h.createCell(1).setCellValue("password");
            h.createCell(2).setCellValue("displayName");
            h.createCell(3).setCellValue("role");
            h.createCell(4).setCellValue("className");
            h.createCell(5).setCellValue("college");
            Row ex = sh.createRow(1);
            ex.createCell(0).setCellValue("2021999");
            ex.createCell(1).setCellValue("初始密码123");
            ex.createCell(2).setCellValue("示例学生");
            ex.createCell(3).setCellValue("STUDENT");
            ex.createCell(4).setCellValue("计算机2101");
            ex.createCell(5).setCellValue("计算机学院");
            wb.write(bos);
            byte[] bytes = bos.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_import_template.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new ByteArrayResource(bytes));
        }
    }

    @PostMapping(value = "/users/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserImportService.ImportResult importUsers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("file") MultipartFile file) {
        return userImportService.importExcel(file, user != null ? user.id() : null);
    }
}
