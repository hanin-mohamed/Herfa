package com.ProjectGraduation.product.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String shortDescription;
    private String longDescription;
    private double price;
    private int quantity;
    private String media;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private Set<OrderDetails> orderDetails;

    @ManyToMany(mappedBy = "savedProducts")
    @JsonIgnore
    private Set<User> savedByUsers;

    @ManyToMany(mappedBy = "favProducts")
    @JsonIgnore
    private Set<User> favByUsers;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments;

    @Transient
    private double discountedPrice;
}