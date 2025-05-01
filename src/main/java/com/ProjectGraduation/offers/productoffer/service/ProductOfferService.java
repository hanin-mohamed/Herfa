package com.ProjectGraduation.offers.productoffer.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.coupons.utils.DiscountType;
import com.ProjectGraduation.offers.productoffer.entity.ProductOffer;
import com.ProjectGraduation.offers.productoffer.repository.ProductOfferRepository;
import com.ProjectGraduation.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductOfferService {

    private final ProductOfferRepository productOfferRepository;

    public double getDiscountedPrice(Product product) {
        LocalDateTime now = LocalDateTime.now();
        Optional<ProductOffer> offerOpt = productOfferRepository
                .findByProducts_IdAndActiveTrueAndStartDateBeforeAndEndDateAfter(
                        product.getId(), now, now);

        return offerOpt.map(productOffer -> calculateDiscountedPrice(product.getPrice(), productOffer)).orElseGet(product::getPrice);
    }



    private double calculateDiscountedPrice(double originalPrice, ProductOffer productOffer) {
        double discountAmount = 0.0;
        switch (productOffer.getDiscountType()) {
            case FIXED_PRICE:
                return Math.max(productOffer.getDiscount(), 0);
            case PERCENTAGE:
                discountAmount = originalPrice * (productOffer.getDiscount() / 100);
                if (productOffer.getMaxDiscount() != null && discountAmount > productOffer.getMaxDiscount()) {
                    discountAmount = productOffer.getMaxDiscount();
                }
                break;
            case AMOUNT:
                discountAmount = productOffer.getDiscount();
                break;
        }
        return Math.max(originalPrice - discountAmount, 0);
    }

    public ProductOffer createOfferForProducts(User creator, List<Product> products, double discount, LocalDateTime start, LocalDateTime end) {
        ProductOffer productOffer = new ProductOffer();
        productOffer.setProducts(products);
        productOffer.setDiscount(discount);
        productOffer.setDiscountType(DiscountType.PERCENTAGE);
        productOffer.setStartDate(start);
        productOffer.setEndDate(end);
        productOffer.setCreatedBy(creator);
        productOffer.setActive(true);
        return productOfferRepository.save(productOffer);
    }


    public List<ProductOffer> getAllOffers() {
        return productOfferRepository.findAll();
    }

    public void deleteOffer(Long id, User currentUser) {
        ProductOffer productOffer = productOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        if (!productOffer.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this offer");
        }
        productOfferRepository.deleteById(id);
    }
}
