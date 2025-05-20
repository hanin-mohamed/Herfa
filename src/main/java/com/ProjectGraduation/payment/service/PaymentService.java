package com.ProjectGraduation.payment.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.service.OrderService;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final JWTService jwtService;

    public String createOrderCheckoutSession(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals(com.ProjectGraduation.order.utils.OrderStatus.PENDING)) {
            throw new IllegalStateException("Order already paid or invalid status.");
        }

        long amountInCents = (long) (order.getTotalPrice() * 100);

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
                                                .build())
                                .build()
                )
                .putMetadata("orderId", String.valueOf(orderId))
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public String createWalletRechargeSession(String token, double amount) throws StripeException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid recharge amount");
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

        Session session = Session.create(params);
        return session.getUrl();
    }

}
