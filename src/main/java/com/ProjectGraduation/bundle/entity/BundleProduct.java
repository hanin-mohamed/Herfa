package com.ProjectGraduation.bundle.entity;

import com.ProjectGraduation.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bundle_id")
    private Bundle bundle;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    public BundleProduct(Bundle savedBundle, Product product, int quantity) {
        this.bundle = savedBundle;
        this.product = product;
        this.quantity = quantity;
    }
}
