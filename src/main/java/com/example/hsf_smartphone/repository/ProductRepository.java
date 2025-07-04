package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // You can add custom query methods here if needed
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
}
