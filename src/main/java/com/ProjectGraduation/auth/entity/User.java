package com.ProjectGraduation.auth.entity;

//import com.ProjectGraduation.Events.entity.Event;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JsonIgnore
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

    @JsonIgnore
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

    // @ManyToMany
    // @JoinTable(
    //     name = "user_interests",
    //     joinColumns = @JoinColumn(name = "user_id"),
    //     inverseJoinColumns = @JoinColumn(name = "merchant_id")
    // )
    // @JsonIgnore
    // private List<Merchant> interestedMerchants = new ArrayList<>();

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

    // Getters and Setters


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Product> getSavedProducts() {
        return savedProducts;
    }

    public void setSavedProducts(List<Product> savedProducts) {
        this.savedProducts = savedProducts;
    }

//    public List<Merchant> getInterestedMerchants() {
//        return interestedMerchants;
//    }
//
//    public void setInterestedMerchants(List<Merchant> interestedMerchants) {
//        this.interestedMerchants = interestedMerchants;
//    }

    public List<Product> getFavProducts() {
        return favProducts;
    }

    public void setFavProducts(List<Product> favProducts) {
        this.favProducts = favProducts;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getOtpExpiration() {
        return otpExpiration;
    }

    public void setOtpExpiration(LocalDateTime otpExpiration) {
        this.otpExpiration = otpExpiration;
    }

    public String getResetOtp() {
        return resetOtp;
    }

    public void setResetOtp(String resetOtp) {
        this.resetOtp = resetOtp;
    }

    public LocalDateTime getResetOtpExpiration() {
        return resetOtpExpiration;
    }

    public void setResetOtpExpiration(LocalDateTime resetOtpExpiration) {
        this.resetOtpExpiration = resetOtpExpiration;
    }

}
