package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.Product;
import com.example.hsf_smartphone.service.ProductService;
import com.example.hsf_smartphone.repository.CategoryRepository;
import com.example.hsf_smartphone.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/list")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product/add";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        product.setCategory(category);
        productService.saveProduct(product);
        return "redirect:/admin/product/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/product/edit";
        } else {
            return "redirect:/admin/product/list";
        }
    }

    @PostMapping("/edit")
    public String editProduct(@ModelAttribute Product product, @RequestParam Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        product.setCategory(category);
        productService.saveProduct(product);
        return "redirect:/admin/product/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/product/list";
    }

    // Public product detail mapping
    @Controller
    @RequestMapping("/products")
    class PublicProductController {
        @Autowired
        private ProductService productService;

        @GetMapping("/detail/{id}")
        public String publicDetail(@PathVariable Long id, Model model) {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int soldCount = productService.getTotalSoldByProductId(id);
                model.addAttribute("product", product);
                model.addAttribute("soldCount", soldCount);
                return "product/detail";
            } else {
                return "redirect:/";
            }
        }
    }
}
