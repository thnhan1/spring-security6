package com.example.springjwtoauth.security.oauth;

import com.example.springjwtoauth.entity.Role;
import com.example.springjwtoauth.entity.User;
import com.example.springjwtoauth.repository.RoleRepository;
import com.example.springjwtoauth.security.CustomUserDetails;
import com.example.springjwtoauth.service.CustomUserDetailsService;
import com.example.springjwtoauth.service.RefreshTokenService;
import com.example.springjwtoauth.entity.RefreshToken;
import com.example.springjwtoauth.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;


    public OAuth2LoginSuccessHandler(JwtService jwtService, CustomUserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String providerName = authToken.getAuthorizedClientRegistrationId();
        String providerId = oauthUser.getName();
        User user = userDetailsService.loadOrCreateOAuthUser(email, providerName, providerId);

        CustomUserDetails userDetails = CustomUserDetails.build(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String token = jwtService.generateToken(claims, userDetails.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\",\"refreshToken\":\"" + refreshToken.getToken() + "\"}");
        response.getWriter().flush();
*/
        Cookie accessTokenCookie = new Cookie("accessToken", token);
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(3600); // 1h

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(3600*7*24);
        response.addCookie(accessTokenCookie);

        response.sendRedirect("http://localhost:5173/oauth-success");
/*
    }
}
