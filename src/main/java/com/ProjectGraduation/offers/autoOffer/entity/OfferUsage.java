package com.ProjectGraduation.offers.autoOffer.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OfferUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offer_id")
    private AutoOffer offer;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private double discountApplied;

    private LocalDateTime usageDate;
}