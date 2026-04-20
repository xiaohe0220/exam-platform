package com.campus.exam.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

public final class AuthPrincipalUtil {

    private AuthPrincipalUtil() {
    }

    public static AuthenticatedUser requireUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        Object p = authentication.getPrincipal();
        if (p instanceof AuthenticatedUser au) {
            return au;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录状态无效");
    }
}
