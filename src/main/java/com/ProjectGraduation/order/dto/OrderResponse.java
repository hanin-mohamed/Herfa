package com.ProjectGraduation.order.dto;

import com.ProjectGraduation.offers.productoffer.dto.AppliedOfferDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private LocalDateTime orderDate;
    private double totalPrice;
    private String status;
    private List<OrderItemDTO> orderDetails;
    private List<AppliedOfferDTO> appliedOffers;
}
