package com.nhanab.accountservice.service;

import com.nhanab.accountservice.models.request.LoginRequest;
import com.nhanab.accountservice.models.request.LogoutRequest;
import com.nhanab.accountservice.models.request.RefreshTokenRequest;
import com.nhanab.accountservice.models.request.SignUpRequest;
import com.nhanab.accountservice.models.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    
    JwtAuthenticationResponse authenticate(LoginRequest request);

    void register(SignUpRequest request);

    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(LogoutRequest logoutRequest);
}
