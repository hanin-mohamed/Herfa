package com.ProjectGraduation.transaction.controller;


import com.ProjectGraduation.transaction.entity.TransactionHistory;
import com.ProjectGraduation.transaction.repository.TransactionHistoryRepository;
import com.ProjectGraduation.transaction.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionHistoryService transactionService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<TransactionHistory>> getOrderTransactions(@PathVariable Long orderId) {
        return ResponseEntity.ok(transactionService.getOrderTransactions(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionHistory>> getUserTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getUserTransactions(userId));
    }
}
