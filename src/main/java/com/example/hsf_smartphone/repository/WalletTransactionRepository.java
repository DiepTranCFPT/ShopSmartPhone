package com.example.hsf_smartphone.repository;

import com.example.hsf_smartphone.entity.WalletTransaction;
import com.example.hsf_smartphone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserOrderByDateDesc(User user);

    List<WalletTransaction> findByUser(User user);
}
