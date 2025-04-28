package com.ProjectGraduation.profile.entity;

import com.ProjectGraduation.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String bio;
    private String profilePictureUrl;
    @OneToOne(cascade = CascadeType.ALL)
    private User user;

}
