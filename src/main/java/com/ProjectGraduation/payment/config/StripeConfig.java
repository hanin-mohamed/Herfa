package com.ProjectGraduation.payment.config;

import com.stripe.Stripe;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

//    String stripeSecretKey = Dotenv.load().get("STRIPE_SECRET_KEY");
    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;
    @PostConstruct
    public void setup() {
        System.out.println("Stripe Secret Key: " + stripeSecretKey);
        Stripe.apiKey = stripeSecretKey;
    }
}
