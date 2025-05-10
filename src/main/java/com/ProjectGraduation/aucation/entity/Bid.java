package com.ProjectGraduation.aucation.entity;


import com.ProjectGraduation.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double bidAmount;
    private LocalDateTime bidTime;

    @ManyToOne
    private User user;

    @ManyToOne
    private AuctionItem auctionItem;
}
