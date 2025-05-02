package com.ProjectGraduation.offers.deals.entity;


import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.deals.utils.DealStatus;
import com.ProjectGraduation.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    private User buyer;
    @ManyToOne
    private Product product;
    private int requestedQuantity;
    private double proposedPrice;
    private Double counterPrice; // if seller proposes another price
    @Enumerated(EnumType.STRING)
    private DealStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
