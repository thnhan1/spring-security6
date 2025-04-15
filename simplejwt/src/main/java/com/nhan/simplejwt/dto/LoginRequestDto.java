package com.nhan.simplejwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public record LoginRequestDto(String username, String password) implements Serializable {

}