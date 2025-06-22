package com.ProjectGraduation.offers.deal.controller;

import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.offers.deal.dto.CounterOfferRequest;
import com.ProjectGraduation.offers.deal.dto.DealRequest;
import com.ProjectGraduation.offers.deal.dto.DealResponse;
import com.ProjectGraduation.offers.deal.service.DealService;
import com.ProjectGraduation.offers.deal.utils.DealStatus;
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
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> createDeal(
            @RequestBody DealRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse deal = dealService.createDeal(request, username);
            return ResponseEntity.ok(new ApiResponse(true, "Deal created successfully", deal));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to create deal: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getBuyerDeals(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            List<DealResponse> deals = dealService.getDealsForBuyer(username);
            return ResponseEntity.ok(new ApiResponse(true, "Buyer deals retrieved", deals));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to retrieve buyer deals: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> getSellerDeals(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            List<DealResponse> deals = dealService.getDealsForSeller(username);
            return ResponseEntity.ok(new ApiResponse(true, "Seller deals retrieved", deals));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to retrieve seller deals: " + ex.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam DealStatus status,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse updated = dealService.updateDealStatus(id, status, username);
            return ResponseEntity.ok(new ApiResponse(true, "Deal status updated", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to update deal status: " + ex.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/counter")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> proposeCounterOffer(
            @PathVariable Long id,
            @RequestBody CounterOfferRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse updated = dealService.proposeCounterOffer(
                    request.getDealId(),
                    request.getCounterPrice(),
                    request.getCounterQuantity(),
                    username);
            return ResponseEntity.ok(new ApiResponse(true, "Counter offer proposed", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to propose counter offer: " + ex.getMessage(), null));
        }
    }
    @PatchMapping("/{id}/seller/accept")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> sellerAcceptDeal(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse updated = dealService.sellerAccept(id, username);
            return ResponseEntity.ok(new ApiResponse(true, "Deal accepted by seller", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to accept deal: " + ex.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/seller/reject")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> sellerRejectDeal(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse updated = dealService.sellerReject(id, username);
            return ResponseEntity.ok(new ApiResponse(true, "Deal rejected by seller", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to reject deal: " + ex.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/buyer/accept")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> acceptCounterPrice(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse updated = dealService.acceptCounterOffer(id, username);
            return ResponseEntity.ok(new ApiResponse(true, "Counter price accepted", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to accept counter price: " + ex.getMessage(), null));
        }
    }
    @PatchMapping("/{id}/buyer/reject")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> buyerRejectCounter(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            DealResponse updated = dealService.buyerRejectCounter(id, username);
            return ResponseEntity.ok(new ApiResponse(true, "Counter offer rejected by buyer", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to reject counter offer: " + ex.getMessage(), null));
        }
    }

}
