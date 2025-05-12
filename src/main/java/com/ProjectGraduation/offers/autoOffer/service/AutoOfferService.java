package com.ProjectGraduation.offers.autoOffer.service;

import com.ProjectGraduation.offers.autoOffer.dto.AutoOfferRequest;
import com.ProjectGraduation.offers.autoOffer.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffer.entity.OfferUsage;
import com.ProjectGraduation.offers.autoOffer.repository.AutoOfferRepository;
import com.ProjectGraduation.offers.autoOffer.repository.OfferUsageRepository;
import com.ProjectGraduation.offers.autoOffer.utils.AutoOfferType;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutoOfferService {
    private final AutoOfferRepository autoOfferRepository;
    private final OfferUsageRepository usageRepo;

    public Optional<AutoOffer> findFirstOrderOffer() {
        return autoOfferRepository.findBestFirstOrderOffer(AutoOfferType.FIRST_ORDER, LocalDateTime.now());
    }

    public Optional<AutoOffer> findMinOrderAmountOffer(double amount, Long productId, Long categoryId) {
        return autoOfferRepository.findBestMinOrderOffer(AutoOfferType.MIN_ORDER_AMOUNT, LocalDateTime.now(), amount, productId, categoryId);
    }

    public Optional<AutoOffer> findLoyaltyPointsOffer(int points, Long productId, Long categoryId) {
        return autoOfferRepository.findBestLoyaltyPointsOffer(AutoOfferType.LOYALTY_POINTS, LocalDateTime.now(), points, productId, categoryId);
    }

    public Optional<AutoOffer> findBuyXGetYOffer(int quantity, Long productId, Long categoryId) {
        return autoOfferRepository.findBestBuyXGetYOffer(AutoOfferType.BUY_X_GET_Y, LocalDateTime.now(), quantity, productId, categoryId);
    }

    @Transactional
    public void recordOfferUsage(AutoOffer offer, User user, Order order, double discount) {
        OfferUsage usage = new OfferUsage();
        usage.setOffer(offer);
        usage.setUser(user);
        usage.setOrder(order);
        usage.setDiscountApplied(discount);
        usage.setUsageDate(LocalDateTime.now());
        usageRepo.save(usage);
    }

    @Transactional
    public AutoOffer createAutoOffer(AutoOfferRequest request, User creator) {
        AutoOffer autoOffer = AutoOffer.builder()
                .type(request.getType())
                .name(request.getName())
                .discount(request.getDiscount() != null ? request.getDiscount() : 0.0)
                .maxDiscount(request.getMaxDiscount())
                .fixedPrice(request.getFixedPrice())
                .minOrderAmount(request.getMinOrderAmount())
                .buyQuantity(request.getBuyQuantity())
                .getQuantity(request.getGetQuantity())
                .requiredPoints(request.getRequiredPoints())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return autoOfferRepository.save(autoOffer);
    }

    @Transactional
    public List<AutoOffer> listAll() {
        return autoOfferRepository.findAll();
    }

    @Transactional
    public AutoOffer getOfferById(Long id) {
        return autoOfferRepository.findById(id).orElseThrow(() -> new RuntimeException("Offer not found"));
    }

    @Transactional
    public AutoOffer update(Long id, AutoOffer data) {
        AutoOffer o = getOfferById(id);
        if (data.getName() != null) o.setName(data.getName());
        if (data.getType() != null) o.setType(data.getType());
        if (data.getDiscount() != null) o.setDiscount(data.getDiscount());
        if (data.getMaxDiscount() != null) o.setMaxDiscount(data.getMaxDiscount());
        if (data.getFixedPrice() != null) o.setFixedPrice(data.getFixedPrice());
        if (data.getBuyQuantity() != null) o.setBuyQuantity(data.getBuyQuantity());
        if (data.getGetQuantity() != null) o.setGetQuantity(data.getGetQuantity());
        if (data.getRequiredPoints() != null) o.setRequiredPoints(data.getRequiredPoints());
        if (data.getEquivalentValue() != null) o.setEquivalentValue(data.getEquivalentValue());
        if (data.getMinOrderAmount() != null) o.setMinOrderAmount(data.getMinOrderAmount());
        if (data.getStartDate() != null) o.setStartDate(data.getStartDate());
        if (data.getEndDate() != null) o.setEndDate(data.getEndDate());
        if (data.getProduct() != null) o.setProduct(data.getProduct());
        if (data.getCategories() != null) o.setCategories(data.getCategories());
        o.setActive(data.isActive());
        return autoOfferRepository.save(o);
    }

    @Transactional
    public void delete(Long id) {
        if (!autoOfferRepository.existsById(id)) throw new RuntimeException("Offer not found");
        autoOfferRepository.deleteById(id);
    }

    @Transactional
    public Optional<AutoOffer> findById(Long id) {
        return autoOfferRepository.findById(id);
    }

    @Transactional
    public boolean existsById(Long id) {
        return autoOfferRepository.existsById(id);
    }

    @Transactional
    public AutoOffer save(AutoOffer offer) {
        return autoOfferRepository.save(offer);
    }
}