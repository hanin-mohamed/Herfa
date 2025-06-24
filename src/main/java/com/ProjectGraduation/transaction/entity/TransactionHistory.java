package com.ProjectGraduation.transaction.entity;

import com.ProjectGraduation.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long orderId;

    private String type;

    private double amount;

    private double balanceAfter;

    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
}
