package com.ProjectGraduation.payment.controller;

import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final OrderRepository orderRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
//            System.out.println("Signature verified - Event type: " + event.getType());
        } catch (Exception e) {
//            System.out.println("Signature verification failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature: " + e.getMessage());
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                System.out.println("Processing 'checkout.session.completed'");

                Gson gson = new Gson();
                JsonObject json = gson.fromJson(payload, JsonObject.class);
                JsonObject sessionJson = json.getAsJsonObject("data").getAsJsonObject("object");

                JsonObject metadata = sessionJson.getAsJsonObject("metadata");

                if (metadata == null || !metadata.has("orderId")) {
                    return ResponseEntity.badRequest().body("Order ID missing in metadata");
                }

                String orderId = metadata.get("orderId").getAsString();

                Order order = orderRepository.findById(Long.parseLong(orderId))
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
//                System.out.println("Order " + orderId + " marked as PAID");

            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing session: " + e.getMessage());
            }
        } else {
            System.out.println("Ignored event type: " + event.getType());
        }

        return ResponseEntity.ok("Webhook received and processed");
    }
}
