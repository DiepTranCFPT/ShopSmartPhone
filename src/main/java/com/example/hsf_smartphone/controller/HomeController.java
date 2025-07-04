package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.Category;
import com.example.hsf_smartphone.entity.Product;
import com.example.hsf_smartphone.repository.CategoryRepository;
import com.example.hsf_smartphone.service.ProductService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    public String home(@RequestParam(value = "categoryId", required = false) Long categoryId,
                      @RequestParam(value = "page", defaultValue = "0") int page,
                      @RequestParam(value = "size", defaultValue = "6") int size,
                      Model model) {
        List<Category> categories = categoryRepository.findAll();
        Page<Product> productPage;
        if (categoryId != null) {
            productPage = productService.getProductsByCategory(categoryId, PageRequest.of(page, size));
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            productPage = productService.getAllProducts(PageRequest.of(page, size));
            model.addAttribute("selectedCategoryId", null);
        }
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        // Add sold count map
        Map<Long, Integer> soldCountMap = new HashMap<>();
        for (Product p : productPage.getContent()) {
            soldCountMap.put(p.getId(), productService.getTotalSoldByProductId(p.getId()));
        }
        model.addAttribute("soldCountMap", soldCountMap);
        return "home";
    }
}
