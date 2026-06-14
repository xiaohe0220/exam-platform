package com.campus.exam;

import com.campus.exam.domain.TeacherInviteCode;
import com.campus.exam.domain.UserAccount;
import com.campus.exam.repository.TeacherInviteCodeRepository;
import com.campus.exam.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.auth.public-registration-enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TeacherInviteCodeRepository teacherInviteCodeRepository;

    @Test
    void publicRegistrationAllowsOneAccountPerStudentOrEmployeeId() throws Exception {
        seedTeacherInvite("INVITE001");

        mockMvc.perform(get("/api/auth/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicRegistrationEnabled").value(true))
                .andExpect(jsonPath("$.registrationInviteRequired").value(true));

        mockMvc.perform(get("/api/auth/registration-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classes").isArray())
                .andExpect(jsonPath("$.classes.length()").value(2))
                .andExpect(jsonPath("$.colleges").isArray());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "SELF_BAD",
                                  "password": "student123",
                                  "role": "STUDENT",
                                  "displayName": "Self Student"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "SELF001",
                                  "password": "student123",
                                  "role": "STUDENT",
                                  "displayName": "张三",
                                  "className": "计算机2101",
                                  "college": "计算机学院",
                                  "inviteCode": "INVITE001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("self001"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "self001",
                                  "password": "student123",
                                  "role": "STUDENT",
                                  "displayName": "李四",
                                  "className": "计算机2101",
                                  "college": "计算机学院",
                                  "inviteCode": "INVITE001"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "SELF001",
                                  "password": "student123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("self001"));
    }

    private void seedTeacherInvite(String code) {
        if (teacherInviteCodeRepository.findByCodeIgnoreCase(code).isPresent()) {
            return;
        }
        UserAccount teacher = userAccountRepository.findByUsernameIgnoreCase("teacher").orElseThrow();
        UserAccount admin = userAccountRepository.findByUsernameIgnoreCase("admin").orElseThrow();
        TeacherInviteCode invite = new TeacherInviteCode();
        invite.setCode(code);
        invite.setTeacherId(teacher.getId());
        invite.setTeacherName(teacher.getDisplayName());
        invite.setCollege(teacher.getCollege());
        invite.setMaxUses(50);
        invite.setUsedCount(0);
        invite.setEnabled(true);
        invite.setCreatedById(admin.getId());
        teacherInviteCodeRepository.save(invite);
    }
}
