package com.ProjectGraduation.aucation.controller;


import com.ProjectGraduation.aucation.dto.BidResponseDTO;
import com.ProjectGraduation.aucation.service.BidService;
import com.ProjectGraduation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    @GetMapping("/auction/{auctionId}/all-bids")
    @PreAuthorize("hasAnyAuthority('ROLE_MERCHANT', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> getAllBidsForAuction(@PathVariable Long auctionId) {
        try {
            List<BidResponseDTO> bids = bidService.getBidsForAuction(auctionId);
            return ResponseEntity.ok(new ApiResponse(true, "All bids fetched for auction " + auctionId, bids));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

}
