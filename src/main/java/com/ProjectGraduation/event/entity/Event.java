package com.ProjectGraduation.event.entity;

import com.ProjectGraduation.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String media;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private User user;

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
}
