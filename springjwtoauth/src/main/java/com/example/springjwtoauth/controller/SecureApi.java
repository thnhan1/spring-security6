package com.example.springjwtoauth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureApi {
    @GetMapping("/secured")
    public String secured() {
        return "secured";
    }
}
