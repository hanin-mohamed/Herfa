package com.ProjectGraduation.aucation.controller;


import com.ProjectGraduation.aucation.entity.Bid;
import com.ProjectGraduation.aucation.service.BidService;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.common.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bids")
public class BidController {

    private final BidService bidService;
    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping("/auctions/{auctionId}")
    public ResponseEntity<ApiResponse> makeBid(@RequestHeader("Authorization") String token, @PathVariable Long auctionId,
                                                @RequestParam double amount) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            Bid bid = bidService.makeBid(auctionId, user, amount);
            return ResponseEntity.ok(new ApiResponse(true, "Bid placed successfully!", bid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to place bid: " + e.getMessage(), null));
        }
    }
}
