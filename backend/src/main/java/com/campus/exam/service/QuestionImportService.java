package com.campus.exam.service;

import com.campus.exam.domain.QuestionType;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.QuestionRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionImportService {

    private final QuestionService questionService;

    public QuestionImportService(QuestionService questionService) {
        this.questionService = questionService;
    }

    public byte[] template() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("questions");
            String[] headers = {
                    "type",
                    "title",
                    "content",
                    "optionsJson",
                    "correctAnswerJson",
                    "difficulty",
                    "chapter",
                    "knowledgePoint",
                    "answerAnalysis"
            };
            Row h = sh.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                h.createCell(i).setCellValue(headers[i]);
                sh.setColumnWidth(i, 22 * 256);
            }
            Row ex = sh.createRow(1);
            ex.createCell(0).setCellValue("SINGLE_CHOICE");
            ex.createCell(1).setCellValue("人工智能的核心目标是什么？");
            ex.createCell(2).setCellValue("请选择最符合题意的一项。");
            ex.createCell(3).setCellValue("[\"模拟人的智能行为\",\"替代所有教师\",\"只用于绘画\",\"只用于数据库\"]");
            ex.createCell(4).setCellValue("\"A\"");
            ex.createCell(5).setCellValue(3);
            ex.createCell(6).setCellValue("人工智能导论");
            ex.createCell(7).setCellValue("AI 基本概念");
            ex.createCell(8).setCellValue("人工智能关注感知、推理、学习和决策等能力。");
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "题库模板生成失败");
        }
    }

    public ImportResult importExcel(AuthenticatedUser user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择 Excel 文件");
        }
        int created = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sh = wb.getSheetAt(0);
            int last = Math.min(sh.getLastRowNum(), 2000);
            for (int i = 1; i <= last; i++) {
                Row r = sh.getRow(i);
                if (r == null || blank(rowText(r, 0)) && blank(rowText(r, 1))) {
                    skipped++;
                    continue;
                }
                try {
                    QuestionType type = QuestionType.valueOf(rowText(r, 0).trim());
                    String title = rowText(r, 1).trim();
                    if (title.isBlank()) {
                        throw new IllegalArgumentException("题干不能为空");
                    }
                    Integer diff = null;
                    String diffText = rowText(r, 5);
                    if (!diffText.isBlank()) {
                        diff = Math.max(1, Math.min(5, (int) Math.round(Double.parseDouble(diffText))));
                    }
                    questionService.create(user, new QuestionRequest(
                            type,
                            title,
                            emptyToNull(rowText(r, 2)),
                            emptyToNull(rowText(r, 3)),
                            type == QuestionType.SHORT_ANSWER ? null : emptyToNull(rowText(r, 4)),
                            diff,
                            emptyToNull(rowText(r, 6)),
                            emptyToNull(rowText(r, 7)),
                            emptyToNull(rowText(r, 8))));
                    created++;
                } catch (Exception e) {
                    errors.add("第 " + (i + 1) + " 行：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Excel 解析失败：" + e.getMessage());
        }
        return new ImportResult(created, skipped, errors.size(), errors.stream().limit(20).toList());
    }

    private static String rowText(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return "";
        }
        DataFormatter fmt = new DataFormatter();
        return fmt.formatCellValue(cell).trim();
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    public record ImportResult(
            int created,
            int skipped,
            int failed,
            List<String> errors
    ) {
    }
}
