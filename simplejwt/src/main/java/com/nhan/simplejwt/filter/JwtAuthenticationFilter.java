package com.nhan.simplejwt.filter;

import com.nhan.simplejwt.security.CustomUserDetails;
import com.nhan.simplejwt.service.CustomUserDetailsService;
import com.nhan.simplejwt.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final CustomUserDetailsService userService;

    private final List<String> ALLOWED_PATHS = Arrays.asList("/login", "/register", "/api/auth", "/h2-console");
    
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        // Skip authentication for allowed paths
        if (isAllowedPath(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);
            
            if (token == null) {
                throw new JwtException("No token found");
            }

            String username = jwtService.getUsernameFromToken(token);
            if (username == null) {
                throw new JwtException("Invalid token: cannot extract username");
            }

            if (!jwtService.validateToken(token, username)) {
                throw new JwtException("Invalid token");
            }

            // ✅ Đúng:
            CustomUserDetails userDetails = (CustomUserDetails) userService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("user {} truy cap vao {}", username, request.getServletPath());

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            handleAuthenticationFailure(response, e.getMessage());
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            handleAuthenticationFailure(response, "Internal server error");
        }
    }

    private void handleAuthenticationFailure(HttpServletResponse response, String errorMessage) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", errorMessage));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean isAllowedPath(String servletPath) {
        return ALLOWED_PATHS.stream()
                .anyMatch(servletPath::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/error");
    }
} 