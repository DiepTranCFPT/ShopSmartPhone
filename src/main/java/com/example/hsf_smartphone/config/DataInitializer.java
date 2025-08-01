package com.example.hsf_smartphone.config;

import com.example.hsf_smartphone.entity.Category;
import com.example.hsf_smartphone.entity.Product;
import com.example.hsf_smartphone.entity.Role;
import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.repository.CategoryRepository;
import com.example.hsf_smartphone.repository.ProductRepository;
import com.example.hsf_smartphone.repository.RoleRepository;
import com.example.hsf_smartphone.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.Collections;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository, CategoryRepository categoryRepository, ProductRepository productRepository, UserRepository userRepository) {
        return args -> {
            // Roles
            if (roleRepository.findByName("ROLE_ADMIN") == null) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }
            if (roleRepository.findByName("ROLE_USER") == null) {
                roleRepository.save(new Role(null, "ROLE_USER"));
            }

            // Categories
            Category smartphone = categoryRepository.findByName("Smartphone");
            if (smartphone == null) {
                smartphone = categoryRepository.save(new Category(null, "Smartphone", null));
            }
            Category tablet = categoryRepository.findByName("Tablet");
            if (tablet == null) {
                tablet = categoryRepository.save(new Category(null, "Tablet", null));
            }

            // Products
            if (productRepository.count() == 0) {
                productRepository.save(new Product(null, "iPhone 15 Pro Max", 34990000.0, "Flagship Apple 2024, 256GB, 3 camera.", "Apple", 10, "https://cdn.tgdd.vn/Products/Images/42/303890/iphone-15-pro-max-blue-thumbnew-600x600.jpg", "Apple", "chiếc", smartphone));
                productRepository.save(new Product(null, "Samsung Galaxy S24 Ultra", 29990000.0, "Flagship Samsung, 256GB, 200MP camera.", "Samsung", 8, "https://cdn.tgdd.vn/Products/Images/42/303890/samsung-galaxy-s24-ultra-thumb-600x600.jpg", "Samsung", "chiếc", smartphone));
                productRepository.save(new Product(null, "Xiaomi 14 Pro", 18990000.0, "Flagship Xiaomi, 512GB, Snapdragon 8 Gen 3.", "Xiaomi", 15, "https://cdn.tgdd.vn/Products/Images/42/303890/xiaomi-14-pro-thumb-600x600.jpg", "Xiaomi", "chiếc", smartphone));
                productRepository.save(new Product(null, "iPad Pro M4", 32990000.0, "Apple iPad Pro 2024, 11 inch, 256GB.", "Apple", 5, "https://cdn.tgdd.vn/Products/Images/522/303890/ipad-pro-m4-thumb-600x600.jpg", "Apple", "chiếc", tablet));
                productRepository.save(new Product(null, "Samsung Galaxy Tab S9 Ultra", 25990000.0, "Samsung tablet, 14.6 inch, 512GB.", "Samsung", 7, "https://cdn.tgdd.vn/Products/Images/522/303890/samsung-galaxy-tab-s9-ultra-thumb-600x600.jpg", "Samsung", "chiếc", tablet));
                productRepository.save(new Product(null, "OPPO Find X7", 15990000.0, "OPPO flagship, 256GB, 50MP camera.", "OPPO", 12, "https://cdn.tgdd.vn/Products/Images/42/303890/oppo-find-x7-thumb-600x600.jpg", "OPPO", "chiếc", smartphone));
                productRepository.save(new Product(null, "Vivo X100 Pro", 17990000.0, "Vivo flagship, 512GB, Zeiss camera.", "Vivo", 9, "https://cdn.tgdd.vn/Products/Images/42/303890/vivo-x100-pro-thumb-600x600.jpg", "Vivo", "chiếc", smartphone));
                productRepository.save(new Product(null, "Realme GT 6", 10990000.0, "Realme, 256GB, 120Hz AMOLED.", "Realme", 20, "https://cdn.tgdd.vn/Products/Images/42/303890/realme-gt-6-thumb-600x600.jpg", "Realme", "chiếc", smartphone));
                productRepository.save(new Product(null, "Asus ROG Phone 8", 24990000.0, "Gaming phone, 512GB, Snapdragon 8 Gen 3.", "Asus", 6, "https://cdn.tgdd.vn/Products/Images/42/303890/asus-rog-phone-8-thumb-600x600.jpg", "Asus", "chiếc", smartphone));
                productRepository.save(new Product(null, "Nokia G60 5G", 6990000.0, "Nokia, 128GB, 5G support.", "Nokia", 18, "https://cdn.tgdd.vn/Products/Images/42/303890/nokia-g60-5g-thumb-600x600.jpg", "Nokia", "chiếc", smartphone));
                productRepository.save(new Product(null, "iPad Air 2024", 18990000.0, "Apple iPad Air, 10.9 inch, 128GB.", "Apple", 10, "https://cdn.tgdd.vn/Products/Images/522/303890/ipad-air-2024-thumb-600x600.jpg", "Apple", "chiếc", tablet));
                productRepository.save(new Product(null, "Lenovo Tab P12 Pro", 13990000.0, "Lenovo tablet, 12.6 inch, 256GB.", "Lenovo", 8, "https://cdn.tgdd.vn/Products/Images/522/303890/lenovo-tab-p12-pro-thumb-600x600.jpg", "Lenovo", "chiếc", tablet));
                productRepository.save(new Product(null, "Xiaomi Pad 6 Pro", 11990000.0, "Xiaomi tablet, 11 inch, 256GB.", "Xiaomi", 14, "https://cdn.tgdd.vn/Products/Images/522/303890/xiaomi-pad-6-pro-thumb-600x600.jpg", "Xiaomi", "chiếc", tablet));
                productRepository.save(new Product(null, "YOMOST Bac Ha&Viet Quat 170ml", 11000.0, "YOMOST Sua Chua Uong Bac Ha&Viet Quat 170ml/1 Hop", "YOMOST", 100, "https://momo.vn/uploads/product1.jpg", "Vinamilk", "hộp", smartphone));
                productRepository.save(new Product(null, "YOMOST Dau Tay 170ml", 11000.0, "YOMOST Sua Chua Uong Dau Tay 170ml/1 Hop", "YOMOST", 100, "https://momo.vn/uploads/product2.jpg", "Vinamilk", "hộp", smartphone));
            }

            // Admin user
            if (userRepository.findByUsername("admin").isEmpty()) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN");
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("1");
                admin.setRoles(Collections.singleton(adminRole));
                admin.setWallet(0.0);
                userRepository.save(admin);
            }
        };
    }
}
