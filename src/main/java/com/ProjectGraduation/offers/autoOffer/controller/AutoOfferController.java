package com.ProjectGraduation.offers.autoOffer.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.offers.autoOffer.dto.AutoOfferRequest;
import com.ProjectGraduation.offers.autoOffer.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffer.service.AutoOfferService;
import com.ProjectGraduation.category.service.CategoryService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auto-offers")
@RequiredArgsConstructor
public class AutoOfferController {

    private final AutoOfferService autoOfferService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> createAutoOffer(
            @RequestBody AutoOfferRequest request, Authentication authentication) {
        String username = authentication.getName();
        User creator = userService.getUserByUsername(username);
        AutoOffer autoOffer = autoOfferService.createAutoOffer(request, creator);
        return ResponseEntity.ok(new ApiResponse(true, "Auto offer created successfully", autoOffer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody AutoOffer data) {
        if (data.getProduct() != null && data.getProduct().getId() != null) {
            Product product = productService.getById(data.getProduct().getId());
            data.setProduct(product);
        } else {
            data.setProduct(null);
        }

        if (data.getCategories() != null && !data.getCategories().isEmpty()) {
            data.getCategories().forEach(category -> {
                if (category.getId() != null) {
                    categoryService.getCategoryById(category.getId());
                }
            });
        } else {
            data.setCategories(null);
        }

        AutoOffer updated = autoOfferService.update(id, data);
        return ResponseEntity.ok(new ApiResponse(true, "Auto offer updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> toggleStatus(
            @PathVariable Long id, @RequestParam boolean active) {
        AutoOffer offer = autoOfferService.getOfferById(id);
        offer.setActive(active);
        AutoOffer updated = autoOfferService.save(offer);
        return ResponseEntity.ok(new ApiResponse(true, "Auto offer status updated", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        AutoOffer offer = autoOfferService.getOfferById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Auto offer fetched successfully", offer));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> listAll() {
        return ResponseEntity.ok(new ApiResponse(true, "All auto offers fetched", autoOfferService.listAll()));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAvailableOffers(
            @RequestBody Map<String, List<Map<String, Object>>> cart) {
        List<AutoOffer> availableOffers = new ArrayList<>();
        List<Map<String, Object>> items = cart.get("items");

        for (Map<String, Object> item : items) {
            Long productId = ((Number) item.get("productId")).longValue();
            int quantity = ((Number) item.get("quantity")).intValue();
            Product product = productService.getById(productId);
            Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
            double subtotal = product.getPrice() * quantity;
            autoOfferService.findFirstOrderOffer().ifPresent(availableOffers::add);
            autoOfferService.findMinOrderAmountOffer(subtotal, productId, categoryId).ifPresent(availableOffers::add);
            autoOfferService.findLoyaltyPointsOffer(0, productId, categoryId).ifPresent(availableOffers::add);
            autoOfferService.findBuyXGetYOffer(quantity, productId, categoryId).ifPresent(availableOffers::add);
        }

        return ResponseEntity.ok(new ApiResponse(true, "Available offers fetched", availableOffers));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        autoOfferService.delete(id);
        return ResponseEntity.ok(new ApiResponse(true, "Auto offer deleted successfully", null));
    }
}
