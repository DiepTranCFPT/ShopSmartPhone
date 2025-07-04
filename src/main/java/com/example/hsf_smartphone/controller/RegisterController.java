package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.Role;
import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.repository.RoleRepository;
import com.example.hsf_smartphone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class RegisterController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, @RequestParam String confirmPassword, Model model) {
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("user", user); // Preserve entered data
            return "register";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            model.addAttribute("user", user); // Preserve entered data
            return "register";
        }
        // No password encoding, just save as plain text
        Role userRole = roleRepository.findByName("ROLE_USER");
        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);
        return "redirect:/login?registerSuccess";
    }
}
