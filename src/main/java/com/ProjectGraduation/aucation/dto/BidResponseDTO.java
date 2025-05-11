package com.ProjectGraduation.aucation.dto;

import lombok.*;

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
    public static BidResponseDTO failed(String message, Long auctionId) {
        return BidResponseDTO.builder()
                .success(false)
                .message(message)
                .auctionId(auctionId)
                .build();
    }
}
