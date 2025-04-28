package com.ProjectGraduation.following.service;

import com.ProjectGraduation.auth.entity.Role;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.following.entity.Following;
import com.ProjectGraduation.following.repository.FollowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private final FollowingRepository followingRepository;

    private final UserRepo userRepo;

    public void followMerchant(Long followerId, Long merchantId) {
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException(" User not found"));

        User merchant = userRepo.findById(merchantId)
                .orElseThrow(() -> new UserNotFoundException("Merchant not found"));

        if (merchant.getRole() != Role.MERCHANT) {
            throw new IllegalArgumentException("You can only follow merchants.");
        }

        boolean alreadyFollowing = followingRepository.findByFollowerAndFollowing(follower, merchant).isPresent();
        if (alreadyFollowing) {
            throw new IllegalStateException("Already following this merchant.");
        }

        Following followMerchant = new Following();
        followMerchant.setFollower(follower);
        followMerchant.setFollowing(merchant);

        followingRepository.save(followMerchant);
    }

    public void unfollowMerchant(Long followerId, Long merchantId) {
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower user not found"));

        User merchant = userRepo.findById(merchantId)
                .orElseThrow(() -> new UserNotFoundException("Merchant not found"));

        Following followMerchant = followingRepository.findByFollowerAndFollowing(follower, merchant)
                .orElseThrow(() -> new IllegalStateException("You are not following this merchant."));

        followingRepository.delete(followMerchant);
    }

    public List<Following> getFollowers(User merchant) {
        return followingRepository.findAllByFollowing(merchant);
    }

    public List<Following> getFollowing(User follower) {
        return followingRepository.findAllByFollower(follower);
    }

    public long getFollowerCount(Long merchantId) {
        User merchant = userRepo.findById(merchantId)
                .orElseThrow(() -> new UserNotFoundException("Merchant not found"));
        return followingRepository.findAllByFollowing(merchant).size();
    }
    public long getFollowingCount(Long followerId) {
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return followingRepository.findAllByFollower(follower).size();
    }
}
