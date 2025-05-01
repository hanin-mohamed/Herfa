package com.ProjectGraduation.offers.autoOffers.entity;


import com.ProjectGraduation.offers.autoOffers.utils.AutoOfferType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auto_offers")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AutoOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double discount;
    private Double maxDiscount;
    private boolean firstOrderOnly;
    private boolean active;
    private Double fixedPrice;

    @Enumerated(EnumType.STRING)
    private AutoOfferType type;

    private Integer buyQuantity;
    private Integer getQuantity;

    private Integer requiredPoints;
    private Double equivalentValue;

    private Double minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
