package com.ProjectGraduation.coupons.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.coupons.utils.DiscountType;
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
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discount;
    private double maxDiscount;
    private Integer availableQuantity;
    private LocalDateTime expiryDate;
    private boolean active = true;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void isActive(boolean active) {
        this.active = active;
    }
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

}
