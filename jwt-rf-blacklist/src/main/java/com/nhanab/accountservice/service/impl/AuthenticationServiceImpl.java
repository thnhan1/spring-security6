package com.nhanab.accountservice.service.impl;

import com.nhanab.accountservice.models.RoleName;
import com.nhanab.accountservice.models.persistence.RefreshToken;
import com.nhanab.accountservice.models.persistence.Role;
import com.nhanab.accountservice.models.persistence.User;
import com.nhanab.accountservice.models.request.LoginRequest;
import com.nhanab.accountservice.models.request.LogoutRequest;
import com.nhanab.accountservice.models.request.RefreshTokenRequest;
import com.nhanab.accountservice.models.request.SignUpRequest;
import com.nhanab.accountservice.models.response.JwtAuthenticationResponse;
import com.nhanab.accountservice.repository.RefreshTokenRepository;
import com.nhanab.accountservice.repository.UserRepository;
import com.nhanab.accountservice.security.jwt.JwtUtils;
import com.nhanab.accountservice.security.service.UserDetailsImpl;
import com.nhanab.accountservice.repository.RoleRepository;
import com.nhanab.accountservice.service.AuthenticationService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HttpServletRequest httpServletRequest;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder, UserRepository userRepository, RoleRepository roleRepository,
            HttpServletRequest request, RefreshTokenRepository refreshTokenRepository,
            UserDetailsService userDetailsService, RedisService redisService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.httpServletRequest = request;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
    }

    @Override
    public JwtAuthenticationResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtUtils.generateToken(userDetails);

        String refreshToken = UUID.randomUUID().toString();
        Instant jwtExpiration = Instant.now().plusMillis(this.jwtUtils.getJwtExpiration());
        Instant refreshExpiration = Instant.now().plusMillis(this.jwtUtils.getRefreshTokenExpiration());

        // refresh token
        RefreshToken refreshTokenO = new RefreshToken();
        refreshTokenO.setToken(refreshToken);
        refreshTokenO.setExpirationDateTime(refreshExpiration);
        refreshTokenO.setDeviceName(httpServletRequest.getHeader("User-Agent"));
        User u = new User();
        u.setId(((UserDetailsImpl) userDetails).getId());
        refreshTokenO.setUser(u);
        refreshTokenRepository.save(refreshTokenO);

        return JwtAuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
                .expiresAt(jwtExpiration).build();
    }

    @Transactional
    @Override
    public void register(SignUpRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new RuntimeException("User already exists");
        });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setFirstName(request.getFirst_name());
        user.setLastName(request.getLast_name());
        Role role = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Collections.singleton(role));

        userRepository.save(user);
    }

    @Transactional
    @Override
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenRequest.getToken())
                .orElseThrow(() -> new ExpiredJwtException(null, null, "Refresh token invalid"));

        if (token.getExpirationDateTime().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new ExpiredJwtException(null, null, "Refresh token expired");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUser().getEmail());

        String accessToken = jwtUtils.generateToken(userDetails);

        // new rf token
        String refreshTokenStr = UUID.randomUUID().toString();
        Instant newRefreshExpiration = Instant.now().plusMillis(this.jwtUtils.getRefreshTokenExpiration());

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(refreshTokenStr);
        newRefreshToken.setExpirationDateTime(newRefreshExpiration);
        newRefreshToken.setDeviceName(httpServletRequest.getHeader("User-Agent"));
        User user = new User();
        user.setId(token.getUser().getId());

        refreshTokenRepository.delete(token);

        newRefreshToken.setUser(user);
        refreshTokenRepository.save(newRefreshToken);

        return JwtAuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshTokenStr)
                .expiresAt(newRefreshExpiration).build();
    }

    @Transactional
    @Override
    public void logout(LogoutRequest logoutRequest) {
        // 1. Xóa refresh token
        int deletedCount = refreshTokenRepository.deleteRefreshTokenByToken(logoutRequest.getRefreshToken());
        if (deletedCount == 0) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 2. Blacklist access token
        String header = httpServletRequest.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String accessToken = header.substring(7);
            Date exp = jwtUtils.extractExpiration(accessToken);
            long ttl = exp.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                redisService.blacklistToken(accessToken, ttl);
                log.info("Access token blacklisted, TTL: {} ms", ttl);
            } else {
                log.info("Access token already expired (no need to blacklist)");
            }
        } else {
            log.warn("No Bearer token found in Authorization header, skipping blacklist");
        }
    }


}
