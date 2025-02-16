package com.ProjectGraduation.order.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.ProjectGraduation.order.entity.OrderRequest;
import com.ProjectGraduation.order.repo.OrderRepo;
import com.ProjectGraduation.order.service.OrderService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JWTService jwtService;
    @Autowired
    private OrderRepo orderRepo ;
    @Autowired
    private ProductRepo repo ;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequest request) {

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = orderService.getUserByUsername(username);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        List<OrderDetails> products = request.getProducts();
        double userBudget = request.getPrice();

        try {
            Order order = orderService.createOrder(user.getId(), products, userBudget);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong: " + e.getMessage());
        }
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public void deleteOrder(@PathVariable Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order Not Found"));
        for (OrderDetails details : order.getOrderDetails()) {
            Product product = details.getProduct();
            product.setQuantity(product.getQuantity() + details.getQuantity());
            repo.save(product);
        }
        orderService.deleteOrder(orderId);
    }
}
