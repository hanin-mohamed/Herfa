package com.ProjectGraduation.offers.deals.controller;

import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.offers.deals.dto.DealRequest;
import com.ProjectGraduation.offers.deals.entity.Deal;
import com.ProjectGraduation.offers.deals.service.DealService;
import com.ProjectGraduation.offers.deals.utils.DealStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;
    private final JWTService jwtService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> createDeal(
            @RequestBody DealRequest request,
            @RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        Deal deal = dealService.createDeal(request, username);
        return ResponseEntity.ok(new ApiResponse(true, "Deal created successfully", deal));
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getBuyerDeals(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        List<Deal> deals = dealService.getDealsForBuyer(username);
        return ResponseEntity.ok(new ApiResponse(true, "Buyer deals retrieved", deals));
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> getSellerDeals(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        List<Deal> deals = dealService.getDealsForSeller(username);
        return ResponseEntity.ok(new ApiResponse(true, "Seller deals retrieved", deals));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam DealStatus status,
            @RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        Deal updated = dealService.updateDealStatus(id, status, username);
        return ResponseEntity.ok(new ApiResponse(true, "Deal status updated", updated));
    }

    @PatchMapping("/{id}/counter")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> proposeCounterPrice(
            @PathVariable Long id,
            @RequestParam double price,
            @RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        Deal updated = dealService.proposeCounterPrice(id, price, username);
        return ResponseEntity.ok(new ApiResponse(true, "Counter price proposed", updated));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> acceptCounterPrice(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        Deal updated = dealService.acceptCounterPrice(id, username);
        return ResponseEntity.ok(new ApiResponse(true, "Counter price accepted", updated));
    }
}
