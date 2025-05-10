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
@Table(name = "auction_item")
public class AuctionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String imageUrl;

    private double startingBid;
    private double currentBid;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean active = true;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User winner;
}
