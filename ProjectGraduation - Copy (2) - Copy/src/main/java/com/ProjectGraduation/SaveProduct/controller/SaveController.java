package com.ProjectGraduation.SaveProduct.controller;

import com.ProjectGraduation.SaveProduct.service.SaveService;
import com.ProjectGraduation.product.entity.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/save")
public class SaveController {

    @Autowired
    private SaveService saveService;

    @PostMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> saveProduct(@PathVariable Long productId,
                                              @RequestHeader("Authorization") String token
                                              ) {
        try {
            saveService.saveProduct(productId, token);
            return ResponseEntity.ok("Product saved successfully!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> unSaveProduct(@PathVariable Long productId,
                                                @RequestHeader("Authorization") String token
                                                ) {
        try {
            saveService.unSaveProduct(productId, token);
            return ResponseEntity.ok("Product unsaved successfully!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<Product>> getAllSavedProducts(@RequestHeader("Authorization") String token) {
        List<Product> savedProducts = saveService.getAllSavedProducts(token);
        return ResponseEntity.ok(savedProducts);
    }
}
