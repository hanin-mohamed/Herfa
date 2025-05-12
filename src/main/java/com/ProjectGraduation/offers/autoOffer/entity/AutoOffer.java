package com.ProjectGraduation.offers.autoOffer.entity;

import com.ProjectGraduation.offers.autoOffer.utils.AutoOfferType;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "auto_offer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private AutoOfferType type;

    private Double discount;
    private Double maxDiscount;
    private Double fixedPrice;
    private Boolean firstOrderOnly;
    private Integer buyQuantity;
    private Integer getQuantity;
    private Integer requiredPoints;
    private Double equivalentValue;
    private Double minOrderAmount;
    private Date startDate;
    private Date endDate;
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToMany
    @JoinTable(
            name = "auto_offer_categories",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}