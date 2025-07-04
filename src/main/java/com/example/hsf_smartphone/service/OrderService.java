package com.example.hsf_smartphone.service;

import com.example.hsf_smartphone.entity.Order;
import com.example.hsf_smartphone.entity.User;
import java.util.Optional;

public interface OrderService {
    Order createOrderFromCart(User user);
}

