package com.nhanab.accountservice.models.response;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse implements Serializable {
    private String accessToken;
    private final String tokenType = "Bearer";
    private Instant expiresAt;
    private String refreshToken;
}
