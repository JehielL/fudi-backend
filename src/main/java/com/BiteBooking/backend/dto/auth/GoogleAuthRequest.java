package com.BiteBooking.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "id Token es obligatorio")
        String idToken
) {
}
