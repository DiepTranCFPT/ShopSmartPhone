package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.Cart;
import com.example.hsf_smartphone.entity.CartItem;
import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.repository.CartRepository;
import com.example.hsf_smartphone.repository.CartItemRepository;
import com.example.hsf_smartphone.repository.UserRepository;
import com.example.hsf_smartphone.entity.Product;
import com.example.hsf_smartphone.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/cart/add/{id}")
    @Transactional
    public String addToCart(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("loginRequired", true);
            return "cart/login-required";
        }
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/";
        }
        Product product = productOpt.get();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);
        session.setAttribute("cartSuccess", true);
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("loginRequired", true);
            return "cart/login-required";
        }
        Cart cart = cartRepository.findByUser(user).orElse(null);
        List<CartItem> cartItems = cart != null ? cartItemRepository.findByCart(cart) : List.of();
        double totalPrice = 0;
        for (CartItem item : cartItems) {
            totalPrice += item.getProduct().getPrice() * item.getQuantity();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", String.format("%,.0f", totalPrice));
        return "cart/index";
    }

    @PostMapping("/cart/remove/{id}")
    @Transactional
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            Optional<Product> productOpt = productRepository.findById(id);
            if (productOpt.isPresent()) {
                cartItemRepository.findByCartAndProduct(cart, productOpt.get())
                    .ifPresent(cartItemRepository::delete);
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{id}")
    @Transactional
    public String updateCartItem(@PathVariable Long id, @RequestParam int quantity, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            Optional<Product> productOpt = productRepository.findById(id);
            if (productOpt.isPresent()) {
                cartItemRepository.findByCartAndProduct(cart, productOpt.get())
                    .ifPresent(cartItem -> {
                        if (quantity > 0) {
                            cartItem.setQuantity(quantity);
                            cartItemRepository.save(cartItem);
                        } else {
                            cartItemRepository.delete(cartItem);
                        }
                    });
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/buynow/{id}")
    @Transactional
    public String buyNow(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("loginRequired", true);
            return "cart/login-required";
        }
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/";
        }
        Product product = productOpt.get();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
        // Xóa các sản phẩm khác khỏi giỏ, chỉ giữ lại sản phẩm này với số lượng 1
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        for (CartItem item : cartItems) {
            if (!item.getProduct().getId().equals(id)) {
                cartItemRepository.delete(item);
            }
        }
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });
        cartItem.setQuantity(1);
        cartItemRepository.save(cartItem);
        // Chuyển hướng sang trang giỏ hàng để đặt hàng
        return "redirect:/cart";
    }

    @GetMapping("/cart/login-required")
    public String loginRequired() {
        return "cart/login-required";
    }
}
