package com.nhan.simplejwt.dto;

import java.io.Serializable;

public record RegisterRequest(String username, String password, String email) implements Serializable {
}
