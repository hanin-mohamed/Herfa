package com.ProjectGraduation.offers.deal.entity;


import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.deal.utils.DealStatus;
import com.ProjectGraduation.order.entity.Order;
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
    private Integer counterQuantity;
    private Double counterPrice; // if seller proposes another price
    @Enumerated(EnumType.STRING)
    private DealStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

}
