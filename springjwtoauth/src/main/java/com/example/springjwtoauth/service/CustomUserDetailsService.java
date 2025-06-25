package com.example.springjwtoauth.service;

import com.example.springjwtoauth.entity.Role;
import com.example.springjwtoauth.entity.User;
import com.example.springjwtoauth.repository.RoleRepository;
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
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return CustomUserDetails.build(user);
    }

    public User loadOrCreateOAuthUser(String email, String providerName, String providerId) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    if (existing.getProviderId() == null) {
                        existing.setProviderId(providerId);
                        existing.setProviderName(providerName);
                        return userRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setUsername(email);
                    user.setProviderId(providerId);
                    HashSet<Role> roles = new HashSet<>();
                    roles.add(roleRepository.findByName("ROLE_USER").orElseThrow());
                    user.setRoles(roles);
                    user.setProviderName(providerName);
                    return userRepository.save(user);
                });
    }
}
