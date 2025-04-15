package com.nhan.simplejwt.security;

import com.nhan.simplejwt.entity.Role;
import com.nhan.simplejwt.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class CustomUserDetails implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;


    public CustomUserDetails(Long id, String username, String email, String password, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = roles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_"+ role.getName())).collect(Collectors.toSet());
    }

    public static CustomUserDetails build(Long id, String username, String email, String password, Set<Role> roles) {
        return new CustomUserDetails(id, username, email, password, roles);
    }

    public static CustomUserDetails build(User user) {
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getRoles());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


}
