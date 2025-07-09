package com.nhanab.accountservice.controller;

import com.nhanab.accountservice.models.request.LoginRequest;
import com.nhanab.accountservice.models.request.LogoutRequest;
import com.nhanab.accountservice.models.request.RefreshTokenRequest;
import com.nhanab.accountservice.models.request.SignUpRequest;
import com.nhanab.accountservice.models.response.JwtAuthenticationResponse;
import com.nhanab.accountservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody SignUpRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public  ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtAuthenticationResponse response = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok("Logged out successfully");
    }
}
