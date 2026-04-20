package com.campus.exam.service;

import com.campus.exam.domain.PlatformSettings;
import com.campus.exam.repository.PlatformSettingsRepository;
import com.campus.exam.web.dto.PlatformSettingsDto;
import com.campus.exam.web.dto.PlatformSettingsUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlatformSettingsService {

    private final PlatformSettingsRepository platformSettingsRepository;

    public PlatformSettingsService(PlatformSettingsRepository platformSettingsRepository) {
        this.platformSettingsRepository = platformSettingsRepository;
    }

    public PlatformSettings getOrCreate() {
        return platformSettingsRepository.findById(1L).orElseGet(() -> {
            PlatformSettings p = new PlatformSettings();
            p.setId(1L);
            return platformSettingsRepository.save(p);
        });
    }

    public PlatformSettingsDto getDto() {
        PlatformSettings p = getOrCreate();
        return toDto(p);
    }

    @Transactional
    public PlatformSettingsDto update(PlatformSettingsUpdateRequest req) {
        if (req.minQuestionDifficulty() > req.maxQuestionDifficulty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "最低难度不能大于最高难度");
        }
        PlatformSettings p = getOrCreate();
        p.setMinQuestionDifficulty(req.minQuestionDifficulty());
        p.setMaxQuestionDifficulty(req.maxQuestionDifficulty());
        p.setDefaultExamDurationMinutes(req.defaultExamDurationMinutes());
        p.setDefaultMaxRetakes(req.defaultMaxRetakes());
        p.setNotifyInAppEnabled(req.notifyInAppEnabled());
        p.setNotifyEmailEnabled(req.notifyEmailEnabled());
        p.setNotifySmsEnabled(req.notifySmsEnabled());
        return toDto(platformSettingsRepository.save(p));
    }

    private static PlatformSettingsDto toDto(PlatformSettings p) {
        return new PlatformSettingsDto(
                p.getMinQuestionDifficulty(),
                p.getMaxQuestionDifficulty(),
                p.getDefaultExamDurationMinutes(),
                p.getDefaultMaxRetakes(),
                Boolean.TRUE.equals(p.getNotifyInAppEnabled()),
                Boolean.TRUE.equals(p.getNotifyEmailEnabled()),
                Boolean.TRUE.equals(p.getNotifySmsEnabled()));
    }
}
