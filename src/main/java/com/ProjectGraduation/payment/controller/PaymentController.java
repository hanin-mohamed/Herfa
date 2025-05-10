package com.ProjectGraduation.payment.controller;


import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping("/checkout-session/{orderId}")
    public ResponseEntity<?> createCheckoutSession(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        long amountInCents = (long) order.getTotalPrice() * 100; // Convert to cents

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:9994/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:9994/payment/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + orderId)
                                                                .build())
                                                .setUnitAmount(amountInCents)

                                                .build()).build()
                )
                .putMetadata("orderId", String.valueOf(orderId))
                .build();
        try {
            Session session = Session.create(params);
            return ResponseEntity.ok(Map.of("url", session.getUrl()));
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Stripe error: " + e.getMessage());
        }

    }
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            String orderId = session.getMetadata().get("orderId");

            Order order = orderRepository.findById(Long.parseLong(orderId))
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            return ResponseEntity.ok(" Payment successful, order updated.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(" Error verifying payment: " + e.getMessage());
        }
    }

    @PostMapping("/wallet/recharge")
    public ResponseEntity<?> createWalletRechargeSession(@RequestHeader("Authorization") String token,
                                                         @RequestParam double amount) {
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid recharge amount");
        }

        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userService.getUserByUsername(username);

        long amountInCents = (long) (amount * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:9994/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:9994/payment/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Wallet Recharge")
                                                                .build())
                                                .setUnitAmount(amountInCents)
                                                .build())
                                .build()
                )
                .putMetadata("recharge", "true")
                .putMetadata("userId", String.valueOf(user.getId()))
                .putMetadata("amount", String.valueOf(amount))
                .build();

        try {
            Session session = Session.create(params);
            return ResponseEntity.ok(Map.of("url", session.getUrl()));
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Stripe error: " + e.getMessage());
        }
    }

}
