package com.example.hsf_smartphone.service;

import com.example.hsf_smartphone.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product saveProduct(Product product);
    void deleteProduct(Long id);
    Page<Product> getAllProducts(Pageable pageable);
    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);
    int getTotalSoldByProductId(Long productId);
}
