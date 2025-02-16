package com.ProjectGraduation.Events.entity;

import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String media;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price; // Optional

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToMany
    @JoinTable(
            name = "event_interests",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> interestedUsers = new HashSet<>();

    public void addInterestedUser(User user) {
        interestedUsers.add(user);
    }

    public void removeInterestedUser(User user) {
        interestedUsers.remove(user);
    }

    // Getters and Setters


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Set<User> getInterestedUsers() {
        return interestedUsers;
    }

    public void setInterestedUsers(Set<User> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }
}
