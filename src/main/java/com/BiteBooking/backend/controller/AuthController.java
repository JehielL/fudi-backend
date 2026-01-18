package com.BiteBooking.backend.controller;

import com.BiteBooking.backend.dto.auth.AuthResponse;
import com.BiteBooking.backend.dto.auth.GoogleAuthRequest;
import com.BiteBooking.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request){
        return ResponseEntity.ok(authenticationService.loginWithGoogle(request));
    }
}
