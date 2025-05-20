package com.ProjectGraduation.payment.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.service.OrderService;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final OrderService orderService;
    private final UserRepository userRepository;

//    private final String endpointSecret = Dotenv.load().get("STRIPE_WEBHOOK_SECRET");
    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpointSecret;


    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature: " + e.getMessage());
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                System.out.println("Processing 'checkout.session.completed'");

                Gson gson = new Gson();
                JsonObject json = gson.fromJson(payload, JsonObject.class);
                JsonObject sessionJson = json.getAsJsonObject("data").getAsJsonObject("object");

                JsonObject metadata = sessionJson.getAsJsonObject("metadata");

                // recharge wallet
                if (metadata != null && metadata.has("recharge") && metadata.get("recharge").getAsString().equals("true")) {
                    String userId = metadata.get("userId").getAsString();
                    double amount = Double.parseDouble(metadata.get("amount").getAsString());

                    User user = userRepository.findById(Long.parseLong(userId))
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    user.setWalletBalance(user.getWalletBalance() + amount);
                    userRepository.save(user);

                    System.out.println("Wallet recharged for user " + userId + " with amount $" + amount);
                    return ResponseEntity.ok("Wallet recharge completed");
                }

                // pay for order
                if (metadata != null && metadata.has("orderId")) {
                    Long orderId = Long.parseLong(metadata.get("orderId").getAsString());
                    orderService.confirmOrderPayment(orderId);
                    return ResponseEntity.ok("Order payment completed");
                }

                return ResponseEntity.badRequest().body("No valid metadata found");

            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error processing session: " + e.getMessage());
            }
        } else {
            System.out.println("Ignored event type: " + event.getType());
        }

        return ResponseEntity.ok("Webhook received and processed");
    }
}
