package com.example.hsf_smartphone.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && Objects.equals(password, userOpt.get().getPassword())) {
            session.setAttribute("user", userOpt.get());
            // Lưu danh sách role name vào session để kiểm tra phân quyền dễ dàng
            java.util.List<String> roleNames = userOpt.get().getRoles().stream().map(r -> r.getName()).toList();
            session.setAttribute("roles", roleNames);
            return "redirect:/";
        } else {
            model.addAttribute("error", true);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
