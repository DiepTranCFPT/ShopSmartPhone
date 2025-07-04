package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.Order;
import com.example.hsf_smartphone.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {
    @Autowired
    private OrderRepository orderRepository;



    @GetMapping("/list")
    public String listOrdersPage(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "admin/order/list";
    }
}
