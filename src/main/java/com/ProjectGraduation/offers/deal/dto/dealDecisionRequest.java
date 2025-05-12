package com.ProjectGraduation.offers.deal.dto;

import com.ProjectGraduation.offers.deal.utils.DealStatus;
import lombok.Data;


@Data
public class dealDecisionRequest {
    private Long dealId;
    private DealStatus decision;
}
