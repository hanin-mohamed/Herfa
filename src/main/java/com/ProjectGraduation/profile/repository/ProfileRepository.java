package com.ProjectGraduation.profile.repository;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);

}
