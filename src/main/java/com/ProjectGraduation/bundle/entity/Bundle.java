package com.ProjectGraduation.bundle.entity;

import com.ProjectGraduation.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.ProjectGraduation.bundle.entity.BundleProduct;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private double bundlePrice;

    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private User merchant;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BundleProduct> products;
}
