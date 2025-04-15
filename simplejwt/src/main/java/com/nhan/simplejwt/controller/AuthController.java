package com.nhan.simplejwt.controller;

import com.nhan.simplejwt.dto.JwtResponse;
import com.nhan.simplejwt.dto.LoginRequestDto;
import com.nhan.simplejwt.dto.RegisterRequest;
import com.nhan.simplejwt.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
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
        authService.register(registerRequest);
        return "User registered successfully";
    }
} 