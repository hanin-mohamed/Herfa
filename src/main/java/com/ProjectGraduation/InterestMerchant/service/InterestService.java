//package com.ProjectGraduation.InterestMerchant.service;
//
//import com.ProjectGraduation.auth.entity.User;
//import com.ProjectGraduation.auth.entity.repo.UserRepo;
//import com.ProjectGraduation.auth.service.JWTService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.NoSuchElementException;
//
//@Service
//public class InterestService {
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @Autowired
//    private MerchantRepo merchantRepo;
//
//    @Autowired
//    private JWTService jwtService;
//
//    @Transactional
//    public void interestMerchant(Long merchantId, String token) {
//        String userUsername = jwtService.getUsername(token.replace("Bearer ", ""));
//        User user = getUserByUsername(userUsername);
//
//        Merchant merchant = merchantRepo.findById(merchantId)
//                .orElseThrow(() -> new NoSuchElementException("Merchant not found with ID: " + merchantId));
//
//        if (user.getInterestedMerchants().contains(merchant)) {
//            throw new IllegalStateException("Merchant is already in your interest list.");
//        }
//
//        user.getInterestedMerchants().add(merchant);
//        userRepo.save(user);
//    }
//
//    @Transactional
//    public void unSaveInterest(Long merchantId, String token) {
//        String userUsername = jwtService.getUsername(token.replace("Bearer ", ""));
//        User user = getUserByUsername(userUsername);
//
//        Merchant merchant = merchantRepo.findById(merchantId)
//                .orElseThrow(() -> new NoSuchElementException("Merchant not found with ID: " + merchantId));
//
//        if (!user.getInterestedMerchants().contains(merchant)) {
//            throw new IllegalStateException("Merchant is not in your interest list.");
//        }
//
//        user.getInterestedMerchants().remove(merchant);
//        userRepo.save(user);
//    }
//    @Transactional
//    public List<User> getUsersInterestedInMerchant(String token) {
//        String merchantUsername = jwtService.getUsername(token.replace("Bearer ", ""));
//        Merchant merchant = getMerchantByUsername(merchantUsername);
//
//        return userRepo.findUsersByInterestedMerchant(merchant.getId());
//    }
//    @Transactional
//    public List<Merchant> getInterestedMerchantsByUser(String token) {
//        String userUsername = jwtService.getUsername(token.replace("Bearer ", ""));
//        User user = getUserByUsername(userUsername);
//
//        return user.getInterestedMerchants();
//    }
//
//    private User getUserByUsername(String username) {
//        return userRepo.findByUsernameIgnoreCase(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }
//
//    private Merchant getMerchantByUsername(String username) {
//        return merchantRepo.findByUsernameIgnoreCase(username)
//                .orElseThrow(() -> new RuntimeException("Merchant not found"));
//    }
//}
