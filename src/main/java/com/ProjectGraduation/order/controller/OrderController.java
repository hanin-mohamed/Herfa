package com.ProjectGraduation.order.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.dto.OrderResponse;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.service.OrderService;
import com.ProjectGraduation.order.utils.OrderStatus;
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
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            OrderResponse order = orderService.createOrder(username, orderRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Order created successfully!", order));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to create order: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getOrderById(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long orderId) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            Order order = orderService.getOrderById(orderId, username);
            return ResponseEntity.ok(new ApiResponse(true, "Order fetched successfully", order));
        } catch (Exception ex) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, "Failed to fetch order: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(new ApiResponse(true, "Order deleted successfully and stock restored!", null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to delete order: " + ex.getMessage(), null));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getMyOrders(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtService.getUsername(token.replace("Bearer ", ""));
            List<Order> orders = orderService.getOrdersByUsername(username);
            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to fetch orders: " + ex.getMessage(), null));
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long orderId,
                                                         @RequestParam OrderStatus status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", updatedOrder));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to update order status: " + ex.getMessage(), null));
        }
    }
}
