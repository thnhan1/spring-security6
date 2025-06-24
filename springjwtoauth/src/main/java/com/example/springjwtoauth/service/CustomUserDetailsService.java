package com.example.springjwtoauth.service;

import com.example.springjwtoauth.entity.User;
import com.example.springjwtoauth.repository.UserRepository;
import com.example.springjwtoauth.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return CustomUserDetails.build(user);
    }

    public User loadOrCreateOAuthUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setUsername(email);
                    user.setPassword("");
                    return userRepository.save(user);
                });
    }
}
