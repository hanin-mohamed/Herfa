package com.ProjectGraduation.order.controller;

import com.ProjectGraduation.auth.api.model.ApiResponse;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JWTService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestHeader("Authorization") String token,
                                                   @RequestBody OrderRequest orderRequest) {
        String username = jwtService.getUsername(token);
        orderService.createOrder(username, orderRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Order created successfully!", null));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getOrderById(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long orderId) {
        String username = jwtService.getUsername(token);
        Order order = orderService.getOrderById(orderId, username);
        return ResponseEntity.ok(new ApiResponse(true, "Order fetched successfully", order));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok(new ApiResponse(true, "Order deleted successfully and stock restored!", null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getMyOrders(@RequestHeader("Authorization") String token) {
        String username = jwtService.getUsername(token);
        List<Order> orders = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
    }
}

