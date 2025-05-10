package com.ProjectGraduation.aucation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private double startingBid;
    private double currentBid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private String createdByUsername;
    private double createdByWallet;
}
