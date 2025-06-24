package com.ProjectGraduation.transaction.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.transaction.entity.TransactionHistory;
import com.ProjectGraduation.transaction.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    public void recordTransaction(User user, Long orderId, String type, double amount, double balanceAfter, String desc) {
        TransactionHistory tx = TransactionHistory.builder()
                .user(user)
                .orderId(orderId)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(desc)
                .build();
        transactionHistoryRepository.save(tx);
    }
}
