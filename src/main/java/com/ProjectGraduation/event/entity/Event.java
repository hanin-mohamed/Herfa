package com.ProjectGraduation.event.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.comment.dto.CommentResponse;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
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

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "event_products",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    public void addInterestedUser(User user) {
        interestedUsers.add(user);
    }

    public void removeInterestedUser(User user) {
        interestedUsers.remove(user);
    }

    public void addProduct(Product product) {
        products.add(product);
        product.getEvents().add(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.getEvents().remove(this);
    }
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setEvent(this);
    }
    public Set<CommentResponse> getCommentResponses() {
        Set<CommentResponse> responses = new HashSet<>();
        for (Comment comment : comments) {
            CommentResponse response = new CommentResponse();
            response.setId(comment.getId());
            response.setContent(comment.getContent());
            response.setCreatedAt(comment.getCreatedAt());
            response.setUpdatedAt(comment.getUpdatedAt());
            response.setUserFirstName(comment.getUser().getFirstName());
            response.setUserLastName(comment.getUser().getLastName());
            response.setUserId(comment.getUser().getId());
            // Set productId if needed
            responses.add(response);
        }
        return responses;
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setEvent(null);
    }

}
