package com.ProjectGraduation.transaction.repository;

import com.ProjectGraduation.transaction.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUserId(Long userId);
    List<TransactionHistory> findByOrderId(Long orderId);

    List<TransactionHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<TransactionHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
