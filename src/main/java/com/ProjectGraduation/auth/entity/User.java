package com.ProjectGraduation.auth.entity;

import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.profile.entity.Profile;
import com.ProjectGraduation.rating.ProfileRating.entity.ProfileRating;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
//    @JsonIgnore
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 1000)
    @JsonIgnore
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

//    @JsonIgnore
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean verified = false;

    private String otp;
    private LocalDateTime otpExpiration;
    private String resetOtp;
    private LocalDateTime resetOtpExpiration;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_saved_products",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonIgnore
    private List<Product> savedProducts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_fav_products",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonIgnore
    private List<Product> favProducts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @OneToMany(mappedBy = "rater")
    @JsonIgnore
    private List<ProfileRating> ratingsGiven;

    @OneToMany(mappedBy = "ratedUser")
    @JsonIgnore
    private List<ProfileRating> ratingsReceived;

    @Column(name = "loyalty_points")
    private int loyaltyPoints = 0;

    @Column(name = "wallet_balance")
    private double walletBalance = 0.0;

    @Column(name = "reserved_balance")
    private double reservedBalance = 0.0;

}