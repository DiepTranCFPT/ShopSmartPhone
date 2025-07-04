package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.CartItem;
import com.example.hsf_smartphone.entity.Cart;
import com.example.hsf_smartphone.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCart(Cart cart);
}

