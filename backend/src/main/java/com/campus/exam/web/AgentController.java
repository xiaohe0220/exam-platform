package com.campus.exam.web;

import com.campus.exam.service.ExamAgentService;
import com.campus.exam.web.dto.AgentChatRequest;
import com.campus.exam.web.dto.AgentChatResponse;
import com.campus.exam.web.dto.AgentStatusDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ExamAgentService examAgentService;

    public AgentController(ExamAgentService examAgentService) {
        this.examAgentService = examAgentService;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public AgentChatResponse chat(@Valid @RequestBody AgentChatRequest req) {
        return examAgentService.respond(req.message());
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public AgentStatusDto status() {
        return examAgentService.status();
    }
}
