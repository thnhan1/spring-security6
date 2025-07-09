package com.nhanab.accountservice.models.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SignUpRequest implements Serializable {
    private String email;
    private String password;
    private String phone;
    private String first_name;
    private String last_name;
    private Integer age;

}
