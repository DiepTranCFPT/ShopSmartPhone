package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.entity.WalletTransaction;
import com.example.hsf_smartphone.repository.UserRepository;
import com.example.hsf_smartphone.repository.WalletTransactionRepository;
import com.example.hsf_smartphone.service.MoMoPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class WalletController {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MoMoPaymentService moMoPaymentService;

    @GetMapping("/wallet")
    public String walletPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<WalletTransaction> walletTransactions = walletTransactionRepository.findByUser(user);
        model.addAttribute("walletTransactions", walletTransactions);
        return "wallet/index";
    }

    @PostMapping("/wallet/recharge")
    public String recharge(@RequestParam double amount, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (amount <= 0) {
            redirectAttributes.addFlashAttribute("error", "Số tiền nạp phải lớn hơn 0");
            return "redirect:/wallet";
        }
        // Generate unique, hard-to-guess orderId for MoMo
        String orderId = "WALLET_" + user.getId() + "_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String orderInfo = "Thanh toan " + user.getId();
        // Ví dụ dữ liệu động cho ví trả sau MoMo (payWithVTS)
        Map<String, Object> extraParams = new java.util.HashMap<>();
        extraParams.put("partnerName", "Tên Đối Tác");
        extraParams.put("storeId", "TẠP HOÁ NGỌC TÚ");
        // Tạo danh sách sản phẩm mẫu (items)
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        java.util.Map<String, Object> item1 = new java.util.HashMap<>();
        item1.put("id", "204727");
        item1.put("name", "YOMOST Bac Ha&Viet Quat 170ml");
        item1.put("description", "YOMOST Sua Chua Uong Bac Ha&Viet Quat 170ml/1 Hop");
        item1.put("category", "beverage");
        item1.put("imageUrl", "https://momo.vn/uploads/product1.jpg");
        item1.put("manufacturer", "Vinamilk");
        item1.put("price", 11000L);
        item1.put("currency", "VND");
        item1.put("quantity", 5);
        item1.put("unit", "hộp");
        item1.put("totalPrice", 55000L);
        item1.put("taxAmount", 200L);
        items.add(item1);
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
        // Gọi MoMo với requestType payWithVTS
        Map<String, Object> momoParams = moMoPaymentService.createPaymentRequest(
            amount,
            orderId,
            orderInfo,
            "payWithVTS",
            extraParams
        );
        try {
            // Send request to MoMo endpoint
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();
            String momoEndpoint = "https://payment.momo.vn/v2/gateway/api/create";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = objectMapper.writeValueAsString(momoParams);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            String payUrl = restTemplate.postForObject(momoEndpoint, request, Map.class).get("payUrl").toString();
            // Save orderId and amount in session for later verification
            session.setAttribute("pendingOrderId", orderId);
            session.setAttribute("pendingAmount", amount);
            return "redirect:" + payUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể kết nối MoMo: " + e.getMessage());
            return "redirect:/wallet";
        }
    }

    @RequestMapping(value = "/wallet/momo-return", method = {RequestMethod.GET, RequestMethod.POST})
    public String momoReturn(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");
        Double amount = (Double) session.getAttribute("pendingAmount");
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if ("0".equals(resultCode)) {
            // Payment successful
            user.setWallet(user.getWallet() + amount);
            userRepository.save(user);
            WalletTransaction tx = new WalletTransaction();
            tx.setUser(user);
            tx.setAmount(amount);
            tx.setType("DEPOSIT");
            tx.setDescription("Nạp tiền vào ví qua MoMo");
            tx.setDate(LocalDateTime.now());
            walletTransactionRepository.save(tx);
            redirectAttributes.addFlashAttribute("success", "Nạp tiền thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại hoặc bị hủy!");
        }
        session.removeAttribute("pendingOrderId");
        session.removeAttribute("pendingAmount");
        return "redirect:/wallet";
    }

    @RequestMapping(value = "/wallet/momo-notify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> momoNotify(@RequestBody Map<String, Object> body) {
        // Optionally handle server-to-server notification from MoMo here
        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
