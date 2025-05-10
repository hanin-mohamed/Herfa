package com.ProjectGraduation.aucation.controller;

import com.ProjectGraduation.aucation.dto.AuctionResponseDTO;
import com.ProjectGraduation.aucation.service.AuctionService;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;
    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> createAuction(
            @RequestHeader("Authorization") String token,
            @RequestParam("image") MultipartFile image,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startingBid") double startingBid,
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("endTime") String endTimeStr) {
        try {
            if (!image.getContentType().startsWith("image/") && !"application/octet-stream".equals(image.getContentType())) {
                throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
            }

            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr.trim());
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr.trim());

            AuctionResponseDTO dto = auctionService.createAuction(user, image, title, description,
                    startingBid, startTime, endTime);

            return ResponseEntity.ok(new ApiResponse(true, "Auction created successfully!", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to create auction: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllAuctions() {
        return ResponseEntity.ok(new ApiResponse(true, "All auctions fetched", auctionService.getAllAuctions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAuctionById(@PathVariable Long id) {
        try {
            AuctionResponseDTO dto = auctionService.getAuctionById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Auction fetched successfully", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/merchant/{username}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> getAuctionsByUsername(@PathVariable String username) {
        List<AuctionResponseDTO> list = auctionService.getAuctionsByMerchant(username);
        return ResponseEntity.ok(new ApiResponse(true, "Auctions by user: " + username, list));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveAuctions() {
        List<AuctionResponseDTO> list = auctionService.getActiveAuctions();
        return ResponseEntity.ok(new ApiResponse(true, "Active auctions fetched", list));
    }
}
