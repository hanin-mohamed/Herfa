package com.ProjectGraduation.rating.ProfileRating.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.rating.ProfileRating.dto.ProfileRatingDTO;
import com.ProjectGraduation.rating.ProfileRating.entity.ProfileRating;
import com.ProjectGraduation.rating.ProfileRating.repository.ProfileRatingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileRatingService {

    private final ProfileRatingRepository profileRatingRepo;
    private final UserRepository userRepository;

    @Transactional
    public void rateUser(Long raterId, Long ratedUserId, int stars, String comment) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }

        User rater = userRepository.findById(raterId)
                .orElseThrow(() -> new UserNotFoundException("Rater user not found"));

        User ratedUser = userRepository.findById(ratedUserId)
                .orElseThrow(() -> new UserNotFoundException("Rated user not found"));

        if (rater.getId().equals(ratedUser.getId())) {
            throw new IllegalArgumentException("You cannot rate yourself");
        }

        if (!"MERCHANT".equalsIgnoreCase(ratedUser.getRole().name())) {
            throw new IllegalArgumentException("Only MERCHANT users can be rated");
        }
        ProfileRating profileRating = profileRatingRepo.findByRaterAndRatedUser(rater, ratedUser)
                .orElse(new ProfileRating());

        profileRating.setRater(rater);
        profileRating.setRatedUser(ratedUser);
        profileRating.setStars(stars);
        profileRating.setComment(comment == null ? "" : comment.trim());

        profileRatingRepo.save(profileRating);
    }


    @Transactional
    public double getAverageRating(Long ratedUserId) {
        User ratedUser = userRepository.findById(ratedUserId)
                .orElseThrow(() -> new UserNotFoundException("Rated user not found"));

        List<ProfileRating> ratings = profileRatingRepo.findByRatedUserOrderByCreatedAtDesc(ratedUser);

        if (ratings.isEmpty()) {
            return 0.0;
        }
        double totalStars = ratings.stream()
                .mapToInt(ProfileRating::getStars)
                .sum();

        return totalStars / ratings.size();
    }

    @Transactional
    public List<ProfileRating> getRatingsForUser(Long ratedUserId) {
        User ratedUser = userRepository.findById(ratedUserId)
                .orElseThrow(() -> new UserNotFoundException("Rated user not found"));

        return profileRatingRepo.findByRatedUserOrderByCreatedAtDesc(ratedUser);
    }
    @Transactional
    public List<ProfileRatingDTO> getAllRatings(Long ratedUserId) {
        User ratedUser = userRepository.findById(ratedUserId)
                .orElseThrow(() -> new UserNotFoundException("Rated user not found"));

        return profileRatingRepo.findByRatedUserOrderByCreatedAtDesc(ratedUser)
                .stream()
                .map(rating -> new ProfileRatingDTO(
                        rating.getStars(),
                        rating.getComment(),
                        rating.getRater().getUsername(),
                        rating.getCreatedAt()
                ))
                .toList();
    }

}
