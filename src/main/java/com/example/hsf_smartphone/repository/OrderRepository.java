package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.Order;
import com.example.hsf_smartphone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    @Query(value = "SELECT CAST(o.order_date AS DATE), SUM(o.total_amount) FROM orders o WHERE o.order_date >= :fromDate GROUP BY CAST(o.order_date AS DATE) ORDER BY CAST(o.order_date AS DATE)", nativeQuery = true)
    List<Object[]> getRevenueByDay(@Param("fromDate") java.time.LocalDateTime fromDate);

    @Query(value = "SELECT CAST(o.order_date AS DATE), COUNT(o.id) FROM orders o WHERE o.order_date >= :fromDate GROUP BY CAST(o.order_date AS DATE) ORDER BY CAST(o.order_date AS DATE)", nativeQuery = true)
    List<Object[]> getOrderCountByDay(@Param("fromDate") java.time.LocalDateTime fromDate);

    @Query(value = "SELECT CAST(o.order_date AS DATE), COALESCE(SUM(oi.quantity),0) FROM orders o JOIN order_items oi ON o.id = oi.order_id WHERE o.order_date >= :fromDate GROUP BY CAST(o.order_date AS DATE) ORDER BY CAST(o.order_date AS DATE)", nativeQuery = true)
    List<Object[]> getProductSoldByDay(@Param("fromDate") java.time.LocalDateTime fromDate);
}
