package com.ProjectGraduation.rating.ProfileRating.repository;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.rating.ProfileRating.entity.ProfileRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRatingRepository extends JpaRepository<ProfileRating, Long> {

    Optional<ProfileRating> findByRaterAndRatedUser(User rater, User ratedUser);
    List<ProfileRating> findByRatedUserOrderByCreatedAtDesc(User ratedUser);


}
