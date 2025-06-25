package com.example.springjwtoauth.service;

import com.example.springjwtoauth.dto.JwtResponse;
import com.example.springjwtoauth.dto.RegisterRequest;
import com.example.springjwtoauth.entity.Role;
import com.example.springjwtoauth.entity.User;
import com.example.springjwtoauth.repository.RoleRepository;
import com.example.springjwtoauth.repository.UserRepository;
import com.example.springjwtoauth.security.CustomUserDetails;
import com.example.springjwtoauth.entity.RefreshToken;
import com.example.springjwtoauth.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public JwtResponse login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", userDetails.getUsername());
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

            String accessToken = jwtService.generateToken(claims, userDetails.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            return new JwtResponse(accessToken, refreshToken.getToken(), userDetails.getUsername(), userDetails.getAuthorities());

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            log.error("Authentication failed for user: {}", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Login error for user {}: {}", username, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed");
        }
    }

    public JwtResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        User user = token.getUser();
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String accessToken = jwtService.generateToken(claims, userDetails.getUsername());
        return new JwtResponse(accessToken, refreshToken, userDetails.getUsername(), userDetails.getAuthorities());
    }

    public String register(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setEmail(registerRequest.email());
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
        return "User registered successfully";
    }
}
