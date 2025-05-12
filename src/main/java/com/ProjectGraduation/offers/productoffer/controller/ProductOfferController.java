package com.ProjectGraduation.offers.productoffer.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.offers.productoffer.entity.ProductOffer;
import com.ProjectGraduation.offers.productoffer.service.ProductOfferService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
public class ProductOfferController {

    private final ProductOfferService productOfferService;
    private final JWTService jwtService;
    private final ProductService productService;
    private final UserService userService;

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> createOfferForProducts(
            @RequestHeader("Authorization") String token,
            @RequestParam List<Long> productIds,
            @RequestParam double discountPercentage,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User merchant = userService.getUserByUsername(username);

        List<Product> products = productService.getProductsByIds(productIds);

        for (Product product : products) {
            if (!product.getUser().getId().equals(merchant.getId())) {
                throw new RuntimeException("Product(s) don't belong to the current merchant");
            }
        }

        ProductOffer offer = productOfferService.createOfferForProducts(merchant, products, discountPercentage, startDate, endDate);

        for (Product product : products) {
            double discounted = productOfferService.getDiscountedPrice(product);
            product.setDiscountedPrice(discounted);
            productService.saveProduct(product);
        }

        return ResponseEntity.ok(new ApiResponse(true, "Offer created successfully", offer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllOffers() {
        List<ProductOffer> offers = productOfferService.getAllOffers();
        return ResponseEntity.ok(new ApiResponse(true, "Offers fetched successfully", offers));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> deleteOffer(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User merchant = userService.getUserByUsername(username);
        productOfferService.deleteOffer(id, merchant);
        return ResponseEntity.ok(new ApiResponse(true, "Offer deleted successfully", null));
    }
}
