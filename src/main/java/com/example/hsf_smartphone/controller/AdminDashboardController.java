package com.example.hsf_smartphone.controller;

import com.example.hsf_smartphone.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class AdminDashboardController {
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof com.example.hsf_smartphone.entity.User) {
            com.example.hsf_smartphone.entity.User user = (com.example.hsf_smartphone.entity.User) userObj;
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
            if (isAdmin) {
                // Thông tin quản lý cho admin
                model.addAttribute("adminFeatures", new String[][]{
                    {"Quản lý danh mục", "/admin/category/list", "Quản lý, thêm, sửa, xóa danh mục sản phẩm"},
                    {"Quản lý sản phẩm", "/admin/product/list", "Quản lý, thêm, sửa, xóa sản phẩm"},
                    {"Quản lý đơn hàng", "/admin/order/list", "Xem và xử lý đơn hàng"},
                    {"Quản lý người dùng", "/admin/user/list", "Xem danh sách, phân quyền người dùng"},
                    {"Thống kê doanh thu", "/admin/statistics", "Xem báo cáo doanh thu, số lượng bán"}
                });

                // Lấy dữ liệu doanh thu 7 ngày gần nhất
                java.time.LocalDateTime fromDate = java.time.LocalDate.now().minusDays(6).atStartOfDay();
                var revenueList = orderRepository.getRevenueByDay(fromDate);
                Map<String, Double> revenueMap = new LinkedHashMap<>();
                Map<String, Integer> orderCountMap = new LinkedHashMap<>();
                Map<String, Integer> productSoldMap = new LinkedHashMap<>();
                for (Object[] row : revenueList) {
                    String date = row[0].toString();
                    Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                    revenueMap.put(date, revenue);
                }
                // Lấy số lượng đơn hàng theo ngày
                var orderCountList = orderRepository.getOrderCountByDay(fromDate);
                for (Object[] row : orderCountList) {
                    String date = row[0].toString();
                    Integer count = row[1] != null ? ((Number) row[1]).intValue() : 0;
                    orderCountMap.put(date, count);
                }
                // Lấy số lượng sản phẩm bán ra theo ngày
                var productSoldList = orderRepository.getProductSoldByDay(fromDate);
                for (Object[] row : productSoldList) {
                    String date = row[0].toString();
                    Integer count = row[1] != null ? ((Number) row[1]).intValue() : 0;
                    productSoldMap.put(date, count);
                }
                model.addAttribute("revenueMap", revenueMap);
                model.addAttribute("orderCountMap", orderCountMap);
                model.addAttribute("productSoldMap", productSoldMap);

                return "admin/dashboard";
            }
        }
        return "redirect:/";
    }
}
