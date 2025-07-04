package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.entity.User;
import com.example.hsf_smartphone.entity.WalletTransaction;
import com.example.hsf_smartphone.repository.UserRepository;
import com.example.hsf_smartphone.repository.WalletTransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class WalletController {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private UserRepository userRepository;

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
        user.setWallet(user.getWallet() + amount);
        userRepository.save(user);
        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setType("DEPOSIT");
        tx.setDescription("Nạp tiền vào ví");
        tx.setDate(LocalDateTime.now());
        walletTransactionRepository.save(tx);
        redirectAttributes.addFlashAttribute("success", "Nạp tiền thành công!");
        return "redirect:/wallet";
    }
}
