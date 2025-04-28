package com.ProjectGraduation.rating.ProfileRating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRatingDTO {
    private int stars;
    private String comment;
    private String raterUsername;
    private Date createdAt;
}
