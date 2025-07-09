package com.nhanab.accountservice.repository;

import com.nhanab.accountservice.models.persistence.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Query("select distinct r from RefreshToken  r where r.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Transactional
    @Modifying
    @Query("delete from RefreshToken r where r.token = :token")
    int deleteRefreshTokenByToken(@Param("token") String token);

}