package com.campus.exam;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void studentAvailableExamsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/student/exams/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void teacherExamsRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/teacher/exams"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void authCapabilitiesIsPublicButSafeByDefault() throws Exception {
        mockMvc.perform(get("/api/auth/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicRegistrationEnabled").value(false))
                .andExpect(jsonPath("$.demoPasswordResetEnabled").value(false));
    }

    @Test
    void publicRegistrationIsDisabledInTestProfile() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newstudent",
                                  "password": "student123",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicPasswordResetIsDisabledInTestProfile() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "student",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePasswordRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "student123",
                                  "newPassword": "newpass123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
