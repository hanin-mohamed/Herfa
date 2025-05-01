package com.ProjectGraduation.offers.productoffer.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.coupons.utils.DiscountType;
import com.ProjectGraduation.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "offers")
public class ProductOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discount;

    private Double maxDiscount;

    private boolean active = true;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @OneToMany
    @JoinColumn(name = "product_id")
    private List<Product> products;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
