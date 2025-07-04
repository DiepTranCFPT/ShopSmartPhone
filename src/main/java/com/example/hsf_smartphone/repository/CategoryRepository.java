package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}

