package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.Cart;
import com.example.hsf_smartphone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

