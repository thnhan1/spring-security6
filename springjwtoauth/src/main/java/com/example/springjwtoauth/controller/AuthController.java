package com.example.springjwtoauth.controller;

import com.example.springjwtoauth.dto.JwtResponse;
import com.example.springjwtoauth.dto.LoginRequestDto;
import com.example.springjwtoauth.dto.RegisterRequest;
import com.example.springjwtoauth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
}
