package com.nhan.simplejwt.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record JwtResponse(String token, String username, Collection<? extends GrantedAuthority> roles
) {
}
