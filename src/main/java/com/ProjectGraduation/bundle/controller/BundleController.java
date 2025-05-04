package com.ProjectGraduation.bundle.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.bundle.dto.BundleRequest;
import com.ProjectGraduation.bundle.dto.BundleResponse;
import com.ProjectGraduation.bundle.service.BundleService;
import com.ProjectGraduation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;
    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> createBundle(
            @RequestHeader("Authorization") String token,
            @RequestBody BundleRequest request) {

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User merchant = userService.getUserByUsername(username);

        BundleResponse bundle = bundleService.createBundle(request, merchant);
        return ResponseEntity.ok(new ApiResponse(true, "Bundle created successfully!", bundle));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllBundles() {
        List<BundleResponse> bundles = bundleService.getAllBundles();
        return ResponseEntity.ok(new ApiResponse(true, "Bundles fetched successfully!", bundles));
    }

    @GetMapping("/merchant")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> getBundlesForMerchant(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        List<BundleResponse> bundles = bundleService.getBundlesForMerchant(username);
        return ResponseEntity.ok(new ApiResponse(true, "Merchant bundles fetched successfully!", bundles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBundleById(@PathVariable Long id) {
        BundleResponse bundle = bundleService.getBundleById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Bundle fetched successfully!", bundle));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> toggleBundleStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        bundleService.toggleBundleStatus(id, active);
        return ResponseEntity.ok(new ApiResponse(true, "Bundle status updated!", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> deleteBundle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User merchant = userService.getUserByUsername(username);

        bundleService.deleteBundle(id, merchant);
        return ResponseEntity.ok(new ApiResponse(true, "Bundle deleted successfully!", null));
    }
}
