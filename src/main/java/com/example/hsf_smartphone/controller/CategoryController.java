package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.Category;
import com.example.hsf_smartphone.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/list")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/category/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category/add";
    }

    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/category/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "admin/category/edit";
        }
        return "redirect:/admin/category/list";
    }

    @PostMapping("/edit")
    public String editCategory(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/category/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin/category/list";
    }
}
