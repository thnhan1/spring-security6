package com.nhanab.accountservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
    @GetMapping("/api/user")
    public Map<String, String> user() {
        return Map.of("message", "User endpoint accessed");
    }

    @GetMapping("/api/admin")
    public Map<String, String> admin() {
        return Map.of("message", "Admin endpoint accessed");
    }
}
