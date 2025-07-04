package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}

