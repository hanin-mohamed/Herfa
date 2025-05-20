package com.ProjectGraduation.payment.controller;

import com.ProjectGraduation.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout-session/{orderId}")
    public ResponseEntity<?> createCheckoutSession(@PathVariable Long orderId) {
        try {
            String url = paymentService.createOrderCheckoutSession(orderId);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment session error: " + e.getMessage());
        }
    }

    @PostMapping("/wallet/recharge")
    public ResponseEntity<?> createWalletRechargeSession(@RequestHeader("Authorization") String token,
                                                         @RequestParam double amount) {
        try {
            String url = paymentService.createWalletRechargeSession(token, amount);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Recharge session error: " + e.getMessage());
        }
    }
}
