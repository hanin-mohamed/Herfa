package com.ProjectGraduation.auth.entity;

import com.ProjectGraduation.Events.entity.Event;
import com.ProjectGraduation.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchant")
public class Merchant   {

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
    private String role ;

    @OneToMany(mappedBy = "merchant")
    @JsonIgnore
    private List<Product> products = new ArrayList<>();


    @ManyToMany(mappedBy = "interestedMerchants")
    @JsonIgnore
    private List<User> interestedUsers = new ArrayList<>();

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<User> getInterestedUsers() {
        return interestedUsers;
    }

    public void setInterestedUsers(List<User> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }
}
