package com.nhanab.accountservice;

import com.nhanab.accountservice.models.RoleName;
import com.nhanab.accountservice.models.persistence.Role;
import com.nhanab.accountservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountServiceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty() && roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            Role roleUser = new Role();
            roleUser.setName(RoleName.ROLE_ADMIN);
            Role roleAdmin = new Role();
            roleAdmin.setName(RoleName.ROLE_USER);
            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);
        }
    }
}
