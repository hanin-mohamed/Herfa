package com.ProjectGraduation.offers.autoOffers.service;

import com.ProjectGraduation.offers.autoOffers.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffers.repository.AutoOfferRepository;
import com.ProjectGraduation.offers.autoOffers.utils.AutoOfferType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutoOfferService {

    private final AutoOfferRepository autoOfferRepository;

    public Optional<AutoOffer> findFirstOrderOffer() {
        return autoOfferRepository.findValidOffersByType(AutoOfferType.FIRST_ORDER, LocalDateTime.now())
                .stream()
                .filter(AutoOffer::isFirstOrderOnly)
                .findFirst();
    }

    public Optional<AutoOffer> findMinOrderAmountOffer(double orderAmount) {
        return autoOfferRepository.findValidOffersByType(AutoOfferType.MIN_ORDER_AMOUNT, LocalDateTime.now())
                .stream()
                .filter(offer -> offer.getMinOrderAmount() != null && orderAmount >= offer.getMinOrderAmount())
                .findFirst();
    }

    public Optional<AutoOffer> findLoyaltyPointsOffer(int userPoints) {
        return autoOfferRepository.findValidOffersByType(AutoOfferType.LOYALTY_POINTS, LocalDateTime.now())
                .stream()
                .filter(offer ->
                        offer.getRequiredPoints() != null &&
                                offer.getEquivalentValue() != null &&
                                userPoints >= offer.getRequiredPoints()
                )
                .findFirst();
    }

    public Optional<AutoOffer> findBuyXGetYOffer(int quantity) {
        return autoOfferRepository.findValidOffersByType(AutoOfferType.BUY_X_GET_Y, LocalDateTime.now())
                .stream()
                .filter(offer ->
                        offer.getBuyQuantity() != null &&
                                offer.getGetQuantity() != null &&
                                quantity >= offer.getBuyQuantity()
                )
                .findFirst();
    }
}
