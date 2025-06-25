package com.example.springjwtoauth.repository;

import com.example.springjwtoauth.entity.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderNameAndProviderId(String providerName, String providerId);
}
