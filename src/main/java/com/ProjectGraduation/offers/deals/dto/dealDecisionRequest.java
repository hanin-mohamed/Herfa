package com.ProjectGraduation.offers.deals.dto;

import com.ProjectGraduation.offers.deals.utils.DealStatus;
import lombok.Data;


@Data
public class dealDecisionRequest {
    private Long dealId;
    private DealStatus decision;
}
