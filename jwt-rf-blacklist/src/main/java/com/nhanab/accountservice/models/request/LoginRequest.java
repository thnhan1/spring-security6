package com.nhanab.accountservice.models.request;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest implements Serializable {
    private String email;
    private String password;
}