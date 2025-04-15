package com.nhan.simplejwt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureApi {
    @GetMapping("/user")
    public String user() {
        return "user can view";
    }
}
