package com.example.springjwtoauth.service;

import com.example.springjwtoauth.entity.Role;
import com.example.springjwtoauth.entity.User;

import com.example.springjwtoauth.entity.UserProvider;
import com.example.springjwtoauth.repository.UserProviderRepository;

import com.example.springjwtoauth.repository.UserRepository;
import com.example.springjwtoauth.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserProviderRepository userProviderRepository) {
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return CustomUserDetails.build(user);
    }

    public User loadOrCreateOAuthUser(String email, String providerName, String providerId) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(email);
            user = userRepository.save(user);
        }

        boolean exists = user.getProviders().stream()
                .anyMatch(p -> providerName.equals(p.getProviderName()) && providerId.equals(p.getProviderId()));

        if (!exists) {
            UserProvider provider = new UserProvider();
            provider.setProviderName(providerName);
            provider.setProviderId(providerId);
            provider.setUser(user);
            user.getProviders().add(provider);
            userProviderRepository.save(provider);
        }
        return user;
    }
}
