package com.ProjectGraduation.aucation.dto;

import lombok.Data;

@Data
public class UpdateAuctionDTO {
    private String title;
    private String description;
    private Double startingBid;
    private String startTime;
    private String endTime;
}
