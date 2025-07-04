package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.entity.Order;
import com.example.hsf_smartphone.service.OrderService;
import com.example.hsf_smartphone.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/order/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/cart/login-required";
        }
        try {
            Order order = orderService.createOrderFromCart(user);
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn: " + order.getId());
            return "redirect:/";
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = e.getClass().getName();
            }
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + msg);
            return "redirect:/cart";
        }
    }

    @GetMapping("/order/my")
    public String myOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("orders", orderRepository.findByUser(user));
        return "order/my";
    }

    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/order/my";
        }
        model.addAttribute("order", order);
        return "order/detail";
    }
}
