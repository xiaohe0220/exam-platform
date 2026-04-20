package com.campus.exam.service;

import com.campus.exam.domain.UserAccount;
import com.campus.exam.domain.UserRole;
import com.campus.exam.repository.UserAccountRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserImportService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserImportService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public record ImportResult(int created, int skipped, List<String> messages) {
    }

    @Transactional
    public ImportResult importExcel(MultipartFile file, Long operatorId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "空文件");
        }
        int created = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sh = wb.getSheetAt(0);
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) {
                    continue;
                }
                String username = trim(fmt.formatCellValue(row.getCell(0)));
                String password = trim(fmt.formatCellValue(row.getCell(1)));
                String displayName = trim(fmt.formatCellValue(row.getCell(2)));
                String roleStr = trim(fmt.formatCellValue(row.getCell(3)));
                String className = trim(fmt.formatCellValue(row.getCell(4)));
                String college = trim(fmt.formatCellValue(row.getCell(5)));
                if (username.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (userAccountRepository.findByUsername(username).isPresent()) {
                    messages.add("第" + (r + 1) + "行：账号已存在 " + username);
                    skipped++;
                    continue;
                }
                UserRole role;
                try {
                    role = UserRole.valueOf(roleStr.toUpperCase());
                } catch (Exception e) {
                    messages.add("第" + (r + 1) + "行：角色无效 " + roleStr);
                    skipped++;
                    continue;
                }
                if (password.isEmpty()) {
                    messages.add("第" + (r + 1) + "行：密码不能为空");
                    skipped++;
                    continue;
                }
                UserAccount u = new UserAccount();
                u.setUsername(username);
                u.setPasswordHash(passwordEncoder.encode(password));
                u.setDisplayName(displayName.isEmpty() ? username : displayName);
                u.setRole(role);
                u.setClassName(className.isEmpty() ? null : className);
                u.setCollege(college.isEmpty() ? null : college);
                u.setEnabled(true);
                userAccountRepository.save(u);
                created++;
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "解析 Excel 失败：" + e.getMessage());
        }
        auditService.log(operatorId, "USER_IMPORT", "created=" + created + " skipped=" + skipped);
        return new ImportResult(created, skipped, messages);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
