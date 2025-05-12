package com.ProjectGraduation.aucation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidResponseDTO {
    private boolean success;
    private String message;

    private double currentBid;
    private String highestBidder;
    private Long auctionId;
    private LocalDateTime bidTime;

    public static BidResponseDTO failed(String message, Long auctionId) {
        return BidResponseDTO.builder()
                .success(false)
                .message(message)
                .auctionId(auctionId)
                .build();
    }
}
