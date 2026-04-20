package com.campus.exam.web;

import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.service.PlatformSettingsService;
import com.campus.exam.web.dto.PlatformSettingsDto;
import com.campus.exam.web.dto.PlatformSettingsUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/platform-settings")
@PreAuthorize("hasAnyRole('ADMIN','COLLEGE_ADMIN')")
public class AdminPlatformController {

    private final PlatformSettingsService platformSettingsService;

    public AdminPlatformController(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    @GetMapping
    public PlatformSettingsDto get() {
        return platformSettingsService.getDto();
    }

    @PutMapping
    public PlatformSettingsDto update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody PlatformSettingsUpdateRequest req) {
        return platformSettingsService.update(req);
    }
}
