package com.ProjectGraduation.offers.productoffers.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.coupons.utils.DiscountType;
import com.ProjectGraduation.offers.productoffers.entity.ProductOffer;
import com.ProjectGraduation.offers.productoffers.entity.ProductOfferUsage;
import com.ProjectGraduation.offers.productoffers.repository.ProductOfferRepository;
import com.ProjectGraduation.offers.productoffers.repository.ProductOfferUsageRepository;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductOfferService {

    private final ProductOfferRepository productOfferRepository;
    private final ProductOfferUsageRepository usageRepository;

    public double getDiscountedPrice(Product product) {
        LocalDateTime now = LocalDateTime.now();
        Optional<ProductOffer> offerOpt = productOfferRepository
                .findByProducts_IdAndActiveTrueAndStartDateBeforeAndEndDateAfter(product.getId(), now, now);

        return offerOpt
                .map(productOffer -> calculateDiscountedPrice(product.getPrice(), productOffer))
                .orElseGet(product::getPrice);
    }

    public Optional<ProductOffer> getProductOffer(Long productId, Long categoryId) {
        return productOfferRepository.findByProductIdOrCategoryId(productId, categoryId, LocalDateTime.now());
    }

    private double calculateDiscountedPrice(double originalPrice, ProductOffer offer) {
        switch (offer.getDiscountType()) {
            case FIXED_PRICE:
                return Math.max(offer.getDiscount(), 0);
            case PERCENTAGE:
                double productDiscount = originalPrice * (offer.getDiscount() / 100);
                if (offer.getMaxDiscount() != null && productDiscount > offer.getMaxDiscount()) {
                    productDiscount = offer.getMaxDiscount();
                }
                return Math.max(originalPrice - productDiscount, 0);
            case AMOUNT:
                return Math.max(originalPrice - offer.getDiscount(), 0);
            default:
                return originalPrice;
        }
    }

    @Transactional
    public ProductOffer createOfferForProducts(User merchant, List<Product> products, double discountPercentage, LocalDateTime startDate, LocalDateTime endDate) {
        ProductOffer offer = ProductOffer.builder()
                .name("Offer for " + products.size() + " products")
                .discountType(DiscountType.PERCENTAGE)
                .discount(discountPercentage)
                .active(true)
                .startDate(startDate)
                .endDate(endDate)
                .products(products)
                .createdBy(merchant)
                .build();
        return productOfferRepository.save(offer);
    }

    @Transactional
    public List<ProductOffer> getAllOffers() {
        return productOfferRepository.findAll();
    }

    @Transactional
    public void deleteOffer(Long id, User merchant) {
        ProductOffer offer = productOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        if (!offer.getCreatedBy().getId().equals(merchant.getId())) {
            throw new RuntimeException("Offer does not belong to the current merchant");
        }
        productOfferRepository.deleteById(id);
    }

    @Transactional
    public void recordOfferUsage(ProductOffer offer, User user, Order order, double discount) {
        ProductOfferUsage usage = ProductOfferUsage.builder()
                .offer(offer)
                .user(user)
                .order(order)
                .discountApplied(discount)
                .usageDate(LocalDateTime.now())
                .build();
        usageRepository.save(usage);
    }
}