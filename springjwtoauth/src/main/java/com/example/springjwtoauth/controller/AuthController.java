package com.example.springjwtoauth.controller;

import com.example.springjwtoauth.dto.JwtResponse;
import com.example.springjwtoauth.dto.LoginRequestDto;
import com.example.springjwtoauth.dto.RegisterRequest;
import com.example.springjwtoauth.service.AuthService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String GOOGLE_AUTH_URL = "/oauth2/authorization/google";


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto.username(), loginRequestDto.password());
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws java.io.IOException {
        response.sendRedirect(GOOGLE_AUTH_URL);
    }
}
