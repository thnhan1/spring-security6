package com.nhanab.accountservice.repository;

import com.nhanab.accountservice.models.RoleName;
import com.nhanab.accountservice.models.persistence.Role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(RoleName name);
}