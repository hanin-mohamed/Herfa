package com.ProjectGraduation.transaction.controller;


import com.ProjectGraduation.transaction.entity.TransactionHistory;
import com.ProjectGraduation.transaction.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionHistoryController {
    private final TransactionHistoryRepository transactionHistoryRepository;
    @GetMapping("/{userId}")
    public List<TransactionHistory> getUserTransactions(@PathVariable Long userId) {
        return transactionHistoryRepository.findByUserId(userId);
    }
}
