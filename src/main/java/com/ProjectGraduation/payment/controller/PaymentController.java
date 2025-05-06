package com.ProjectGraduation.payment.controller;


import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;

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


}
