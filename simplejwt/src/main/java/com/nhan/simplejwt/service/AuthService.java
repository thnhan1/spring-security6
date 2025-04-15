package com.nhan.simplejwt.service;

import com.nhan.simplejwt.dto.JwtResponse;
import com.nhan.simplejwt.dto.RegisterRequest;
import com.nhan.simplejwt.entity.Role;
import com.nhan.simplejwt.entity.User;
import com.nhan.simplejwt.repository.RoleRepository;
import com.nhan.simplejwt.repository.UserRepository;
import com.nhan.simplejwt.security.CustomUserDetails;
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
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public JwtResponse login(String username, String password) {
        try {
            // Kiểm tra username tồn tại
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

            // Kiểm tra password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }

            // Tạo authentication token
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy thông tin user details
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Tạo JWT claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", userDetails.getUsername());
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            claims.put("iat", new Date());
            claims.put("exp", new Date(System.currentTimeMillis() + 86400000)); // 24 hours

            // Generate JWT token
            String accessToken = jwtService.generateToken(claims, userDetails.getUsername());

            // Trả về response
            return new JwtResponse(
                    accessToken,
                    userDetails.getUsername(),
                    userDetails.getAuthorities()
            );

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext(); // Clear context nếu authentication thất bại
            log.error("Authentication failed for user: {}", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } catch (Exception e) {
            SecurityContextHolder.clearContext(); // Clear context nếu có lỗi
            log.error("Login error for user {}: {}", username, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed");
        }
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