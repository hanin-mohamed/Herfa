package com.ProjectGraduation.order.controller;

import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JWTService jwtService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestHeader("Authorization") String token,
                                              @RequestBody OrderRequest orderRequest) {
        String username = jwtService.getUsername(token);
        orderService.createOrder(username, orderRequest);
        return ResponseEntity.ok("Order created successfully!");
    }
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getOrderById(@RequestHeader("Authorization") String token,
                                          @PathVariable Long orderId) {
        String username = jwtService.getUsername(token);
        return ResponseEntity.ok(orderService.getOrderById(orderId, username));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order deleted successfully and stock restored!");
    }
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getMyOrders(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token);
        return ResponseEntity.ok(orderService.getOrdersByUsername(username));
    }

}
