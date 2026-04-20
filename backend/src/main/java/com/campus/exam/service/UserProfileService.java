package com.campus.exam.service;

import com.campus.exam.domain.UserAccount;
import com.campus.exam.repository.UserAccountRepository;
import com.campus.exam.security.AuthenticatedUser;
import com.campus.exam.web.dto.UserProfileDto;
import com.campus.exam.web.dto.UserProfileUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

    private static final ObjectMapper M = new ObjectMapper();

    private final UserAccountRepository userAccountRepository;

    public UserProfileService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserProfileDto get(AuthenticatedUser user) {
        UserAccount u = userAccountRepository.findById(user.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return toDto(u);
    }

    public UserProfileDto update(AuthenticatedUser user, UserProfileUpdateRequest req) {
        UserAccount u = userAccountRepository.findById(user.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.displayName() != null) {
            String dn = req.displayName().trim();
            if (dn.isEmpty() || dn.length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "显示名长度 1–100");
            }
            u.setDisplayName(dn);
        }
        if (req.personalNote() != null) {
            if (req.personalNote().length() > 300) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "个性签名最多 300 字");
            }
            u.setPersonalNote(req.personalNote());
        }
        if (req.settingsJson() != null) {
            String s = req.settingsJson().trim();
            if (!s.isEmpty()) {
                try {
                    JsonNode n = M.readTree(s);
                    if (!n.isObject()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "settingsJson 须为 JSON 对象");
                    }
                } catch (ResponseStatusException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "settingsJson 不是合法 JSON");
                }
            }
            u.setSettingsJson(s.isEmpty() ? null : s);
        }
        userAccountRepository.save(u);
        return toDto(u);
    }

    private static UserProfileDto toDto(UserAccount u) {
        return new UserProfileDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole().name(),
                u.getClassName(),
                u.getCollege(),
                u.getPersonalNote(),
                u.getSettingsJson());
    }
}
