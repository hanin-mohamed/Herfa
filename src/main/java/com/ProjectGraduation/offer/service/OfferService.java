package com.ProjectGraduation.offer.service;


import com.ProjectGraduation.coupons.utils.DiscountType;
import com.ProjectGraduation.offer.entity.Offer;
import com.ProjectGraduation.offer.repository.OfferRepository;
import com.ProjectGraduation.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Math.max;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;

    public double getDiscountedPrice(Product product) {
        List<Offer> offers = offerRepository.findByProductAndActiveTrue(product);
        LocalDateTime now = LocalDateTime.now();

        for (Offer offer : offers) {
            if (isOfferValid(offer, now)) {
                return calculateDiscountedPrice(product.getPrice(), offer);
            }
        }
        return product.getPrice();
    }

    private boolean isOfferValid(Offer offer, LocalDateTime now) {
        if (!offer.isActive()) return false;
        if (offer.getStartDate() != null && now.isBefore(offer.getStartDate())) return false;
        if (offer.getEndDate() != null && now.isAfter(offer.getEndDate())) return false;
        return true;
    }
    private double calculateDiscountedPrice(double originalPrice, Offer offer) {
        if (offer.getDiscountType() == DiscountType.FIXED_PRICE) {
            return max(offer.getDiscount(), 0);
        } else if (offer.getDiscountType() == DiscountType.PERCENTAGE) {
            double discountAmount = originalPrice * (offer.getDiscount() / 100);
            if (offer.getMaxDiscount() != null && discountAmount > offer.getMaxDiscount()) {
                discountAmount = offer.getMaxDiscount();
            }
            return max(originalPrice - discountAmount, 0);
        } else if (offer.getDiscountType() == DiscountType.AMOUNT) {
            return max(originalPrice - offer.getDiscount(), 0);
        }
        return originalPrice;
    }
}
