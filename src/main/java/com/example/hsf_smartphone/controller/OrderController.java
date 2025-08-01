package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.entity.Order;
import com.example.hsf_smartphone.service.OrderService;
import com.example.hsf_smartphone.repository.OrderRepository;
import com.example.hsf_smartphone.service.MoMoPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MoMoPaymentService moMoPaymentService;

    @PostMapping("/order/checkout")
    public String checkout(@RequestParam(value = "paymentMethod", required = false) String paymentMethod,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/cart/login-required";
        }
        try {
            Order order = orderService.createOrderFromCart(user);
            String orderId = "ORDER_" + order.getId();
            String orderInfo = "Thanh toán đơn hàng " + order.getId();
            if (paymentMethod == null) paymentMethod = "captureWallet"; // mặc định ví thường
            Map<String, Object> extraParams = new HashMap<>();
            if (paymentMethod.equals("payWithVTS")) {
                // Sử dụng đúng thông tin sản phẩm thực tế từ order.getItems()
                extraParams.put("partnerName", "Tên Đối Tác Production"); // Thay bằng tên đối tác production MoMo cấp
                extraParams.put("storeId", "StoreIdProduction"); // Thay bằng storeId production MoMo cấp
                // Nếu MoMo cấp subPartnerCode thì truyền vào
                // extraParams.put("subPartnerCode", "SubPartnerCodeProduction");
                // Nếu MoMo cấp orderGroupId thì truyền vào
                // extraParams.put("orderGroupId", "OrderGroupIdProduction");
                // Nếu muốn kiểm soát autoCapture
                // extraParams.put("autoCapture", true);
                java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
                for (var orderItem : order.getItems()) {
                    var product = orderItem.getProduct();
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("id", String.valueOf(product.getId()));
                    item.put("name", product.getName());
                    item.put("description", product.getDescription());
                    item.put("category", product.getBrand() != null ? product.getBrand() : "");
                    item.put("imageUrl", product.getImageUrl());
                    item.put("manufacturer", product.getManufacturer() != null ? product.getManufacturer() : "");
                    item.put("price", product.getPrice().longValue());
                    item.put("currency", "VND");
                    item.put("quantity", orderItem.getQuantity());
                    item.put("unit", product.getUnit() != null ? product.getUnit() : "");
                    item.put("totalPrice", product.getPrice().longValue() * orderItem.getQuantity());
                    item.put("taxAmount", 0L); // Sửa lại nếu có thuế
                    items.add(item);
                }
                extraParams.put("items", items);
                // userInfo
                java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
                userInfo.put("name", user.getFullName() != null ? user.getFullName() : "");
                userInfo.put("phoneNumber", user.getPhone() != null ? user.getPhone() : "");
                userInfo.put("email", user.getEmail() != null ? user.getEmail() : "");
                extraParams.put("userInfo", userInfo);
                // extraData (base64 json)
                String extraDataJson = "{\"username\": \"" + user.getUsername() + "\"}";
                String extraDataBase64 = java.util.Base64.getEncoder().encodeToString(extraDataJson.getBytes());
                extraParams.put("extraData", extraDataBase64);
            }
            Map<String, Object> momoParams = moMoPaymentService.createPaymentRequest(order.getTotalAmount(), orderId, orderInfo, paymentMethod, extraParams);
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();
            String momoEndpoint = "https://payment.momo.vn/v2/gateway/api/create";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = objectMapper.writeValueAsString(momoParams);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            String payUrl = restTemplate.postForObject(momoEndpoint, request, Map.class).get("payUrl").toString();
            session.setAttribute("pendingOrderId", order.getId());
            return "redirect:" + payUrl;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = e.getClass().getName();
            }
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + msg);
            return "redirect:/cart";
        }
    }

    @RequestMapping(value = "/order/momo-return", method = {RequestMethod.GET, RequestMethod.POST})
    public String momoOrderReturn(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        String resultCode = params.get("resultCode");
        String orderIdStr = params.get("orderId");
        Long orderId = null;
        if (orderIdStr != null && orderIdStr.startsWith("ORDER_")) {
            orderId = Long.parseLong(orderIdStr.replace("ORDER_", ""));
        }
        if (orderId == null) {
            redirectAttributes.addFlashAttribute("error", "Không xác định được đơn hàng!");
            return "redirect:/order/my";
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
            return "redirect:/order/my";
        }
        if ("0".equals(resultCode)) {
            // Payment successful
            order.setStatus("PAID");
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("success", "Thanh toán thành công cho đơn hàng " + order.getId() + "!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại hoặc bị hủy!");
        }
        session.removeAttribute("pendingOrderId");
        return "redirect:/order/my";
    }

    @RequestMapping(value = "/order/momo-notify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> momoOrderNotify(@RequestBody Map<String, Object> body) {
        // Optionally handle server-to-server notification from MoMo here
        return new ResponseEntity<>("", HttpStatus.OK);
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
