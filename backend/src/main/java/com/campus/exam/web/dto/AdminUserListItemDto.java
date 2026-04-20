package com.campus.exam.web.dto;

import com.campus.exam.domain.UserAccount;

/**
 * 管理端用户列表（不直接序列化实体，避免字段变更/代理类导致的序列化问题）。
 */
public record AdminUserListItemDto(
        Long id,
        String username,
        String displayName,
        String role,
        String className,
        String college,
        String email,
        boolean enabled
) {
    public static AdminUserListItemDto from(UserAccount u) {
        boolean en = u.getEnabled() == null || Boolean.TRUE.equals(u.getEnabled());
        return new AdminUserListItemDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getClassName(),
                u.getCollege(),
                u.getEmail(),
                en);
    }
}
