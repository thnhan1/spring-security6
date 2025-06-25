package com.example.springjwtoauth.repository;

import com.example.springjwtoauth.entity.RefreshToken;
import com.example.springjwtoauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
