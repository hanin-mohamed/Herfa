package com.ProjectGraduation.refundOrder.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.refundOrder.entity.RefundReasonType;
import com.ProjectGraduation.refundOrder.entity.RefundRequest;
import com.ProjectGraduation.refundOrder.service.RefundRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundRequestController {

    private final RefundRequestService refundRequestService;
    private final UserService userService;

    @PostMapping("/request/{orderId}")
    public ResponseEntity<ApiResponse> requestRefund(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        User user = userService.getUserByUsername(authentication.getName());

        RefundReasonType reasonType = RefundReasonType.valueOf((String) body.get("reasonType"));
        String message = (String) body.get("message");
        List<String> images = (List<String>) body.getOrDefault("images", List.of());

        RefundRequest request = refundRequestService.createRefundRequest(user, orderId, reasonType, message, images);

        return ResponseEntity.ok(new ApiResponse(true, "Refund request created", request));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse> getMyRequests(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<RefundRequest> requests = refundRequestService.getRequestsForUser(user);
        return ResponseEntity.ok(new ApiResponse(true, "Fetched user refund requests", requests));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllRequests() {
        List<RefundRequest> requests = refundRequestService.getAllRequestsForAdmin();
        return ResponseEntity.ok(new ApiResponse(true, "Fetched all refund requests", requests));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<ApiResponse> approveRefund(@PathVariable Long id) {
        RefundRequest request = refundRequestService.approveRefund(id);
        return ResponseEntity.ok(new ApiResponse(true, "Refund approved", request));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<ApiResponse> rejectRefund(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        RefundRequest request = refundRequestService.rejectRefund(id, reason);
        return ResponseEntity.ok(new ApiResponse(true, "Refund rejected", request));
    }
}
