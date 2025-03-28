//package com.ProjectGraduation.InterestMerchant.controller;
//
//import com.ProjectGraduation.InterestMerchant.service.InterestService;
//import com.ProjectGraduation.auth.entity.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/interest")
//public class InterestController {
//
//    @Autowired
//    private InterestService service;
//
//    @PostMapping("/{merchantId}")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
//    public ResponseEntity<String> interestMerchant(@PathVariable Long merchantId,
//                                                   @RequestHeader("Authorization") String token) {
//        try {
//            service.interestMerchant(merchantId, token);
//            return ResponseEntity.ok("Merchant added to interested list successfully!");
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(400).body("Error: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{merchantId}")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
//    public ResponseEntity<String> unSaveInterest(@PathVariable Long merchantId,
//                                                 @RequestHeader("Authorization") String token) {
//        try {
//            service.unSaveInterest(merchantId, token);
//            return ResponseEntity.ok("Merchant removed from interest list successfully!");
//        } catch (IllegalStateException e) {
//            return ResponseEntity.status(400).body("Error: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/merchant")
//    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
//    public ResponseEntity<List<User>> getUsersInterestedInMerchant(@RequestHeader("Authorization") String token) {
//        return ResponseEntity.ok(service.getUsersInterestedInMerchant(token));
//    }
//
//    @GetMapping("/user")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
//    public ResponseEntity<List<Merchant>> getInterestedMerchants(@RequestHeader("Authorization") String token) {
//        return ResponseEntity.ok(service.getInterestedMerchantsByUser(token));
//    }
//}
