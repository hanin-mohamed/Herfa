package com.ProjectGraduation.comment.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
public class Comment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    private String content ;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne()
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    public Comment() {
    }

    public Comment(String content , User user, Product product) {
         this.content = content;
        this.createdAt = LocalDateTime.now();
        this.user = user;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
