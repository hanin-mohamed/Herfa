package com.ProjectGraduation.product.entity;

import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String shortDescription;
    private String longDescription;
    private double price;
    private double quantity;
    private String media;

    private Boolean active ;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
//    @JsonBackReference
    @JsonIgnore
    private Merchant merchant;


    @ManyToMany(mappedBy = "product")
    @JsonIgnore
    private Set<OrderDetails> orderDetails = new HashSet<>();

    @ManyToMany(mappedBy = "savedProducts")
    @JsonIgnore
    private Set<User> savedByUsers = new HashSet<>();

    @ManyToMany(mappedBy = "favProducts")
    @JsonIgnore
    private Set<User> favByUsers = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();



    public Product() {
    }

    public Product(Long id, String name, String shortDescription, String longDescription, double price, double quantity, String media, Boolean active, Category category, Merchant merchant, Set<OrderDetails> orderDetails, Set<User> savedByUsers) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.price = price;
        this.quantity = quantity;
        this.media = media;
        this.active = active;
        this.category = category;
        this.merchant = merchant;
        this.orderDetails = orderDetails;
        this.savedByUsers = savedByUsers;
    }

    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Set<OrderDetails> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(Set<OrderDetails> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Set<User> getSavedByUsers() {
        return savedByUsers;
    }

    public void setSavedByUsers(Set<User> savedByUsers) {
        this.savedByUsers = savedByUsers;
    }
}
