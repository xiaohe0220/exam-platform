package com.campus.exam.security;

public record AuthenticatedUser(Long id, String username, String role) {
}
