package com.BiteBooking.backend.dto.auth;

import com.BiteBooking.backend.model.Role;

public record AuthResponse(
        String token,
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        String imgUser) {
}
