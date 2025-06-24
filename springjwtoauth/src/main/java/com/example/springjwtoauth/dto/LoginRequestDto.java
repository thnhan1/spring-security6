package com.example.springjwtoauth.dto;

import java.io.Serializable;

public record LoginRequestDto(String username, String password) implements Serializable {
}
