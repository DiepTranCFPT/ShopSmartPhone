package com.example.hsf_smartphone.service;

import com.example.hsf_smartphone.entity.*;
import com.example.hsf_smartphone.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Order createOrderFromCart(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        // Luôn chuyển Set<CartItem> sang List để thao tác an toàn
        List<CartItem> items = (cart != null && cart.getItems() != null) ? new java.util.ArrayList<>(cart.getItems()) : List.of();
        if (cart == null || items.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống hoặc không hợp lệ");
        }
        // Bỏ qua cartItem có product null
        List<CartItem> validItems = items.stream()
            .filter(item -> item.getProduct() != null)
            .toList();
        if (validItems.isEmpty()) {
            throw new IllegalStateException("Tất cả sản phẩm trong giỏ đã bị xóa hoặc không hợp lệ");
        }
        double total = validItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        if (user.getWallet() == null || user.getWallet() < total) {
            throw new IllegalStateException("Số dư không đủ để thanh toán đơn hàng");
        }
        user.setWallet(user.getWallet() - total);
        userRepository.save(user);
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);
        order = orderRepository.save(order);
        // Duyệt qua bản copy để tránh ConcurrentModificationException
        List<CartItem> validItemsCopy = new java.util.ArrayList<>(validItems);
        try {
            for (CartItem cartItem : validItemsCopy) {
                if (cartItem.getProduct() == null) {
                    throw new IllegalStateException("Có sản phẩm trong giỏ đã bị xóa khỏi hệ thống. Vui lòng kiểm tra lại giỏ hàng.");
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                orderItemRepository.save(orderItem);
                Product product = cartItem.getProduct();
                int newQuantity = product.getQuantity() - cartItem.getQuantity();
                if (newQuantity < 0) {
                    throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng trong kho.");
                }
                product.setQuantity(newQuantity);
                productRepository.save(product);
            }
            // Xóa cart item: clear collection trước, sau đó xóa từng item bằng repository
            if (cart.getItems() != null) cart.getItems().clear();
            for (CartItem cartItem : validItemsCopy) {
                cartItemRepository.deleteById(cartItem.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = e.getClass().getName();
            }
            throw new IllegalStateException("Lỗi hệ thống khi tạo đơn hàng: " + msg, e);
        }
        return order;
    }
}
