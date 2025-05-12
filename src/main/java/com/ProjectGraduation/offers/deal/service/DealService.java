package com.ProjectGraduation.offers.deal.service;


import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.offers.deal.dto.DealRequest;
import com.ProjectGraduation.offers.deal.entity.Deal;
import com.ProjectGraduation.offers.deal.repository.DealRepository;
import com.ProjectGraduation.offers.deal.utils.DealStatus;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {


    private final UserRepository userRepository;
    private final ProductService productService;
    private final DealRepository dealRepository;
    private final UserService userService;
    @Transactional
    public Deal createDeal(DealRequest request, String username) {
        User buyer = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.getById(request.getProductId());
        if (product == null || !product.isActive()) {
            throw new RuntimeException("Invalid product");
        }

        if (product.getUser().getId().equals(buyer.getId())) {
            throw new RuntimeException("You cannot create a deal on your own product");
        }

        Deal deal = new Deal();
        deal.setBuyer(buyer);
        deal.setProduct(product);
        deal.setRequestedQuantity(request.getRequestedQuantity());
        deal.setProposedPrice(request.getProposedPrice());
        deal.setCounterPrice(null);
        deal.setStatus(DealStatus.PENDING);

        return dealRepository.save(deal);
    }

    public List<Deal> getDealsForBuyer(String username) {
        User buyer = userService.getUserByUsername(username);
        return dealRepository.findByBuyer(buyer);
    }
    public List<Deal> getDealsForSeller(String username) {
        User seller = userService.getUserByUsername(username);
        return dealRepository.findByProduct_User(seller);
    }

    public Deal updateDealStatus(Long dealId, DealStatus status, String sellerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getProduct().getUser().getUsername().equalsIgnoreCase(sellerUsername)) {
            throw new RuntimeException("You are not authorized to update this deal");
        }

        deal.setStatus(status);
        return dealRepository.save(deal);
    }

    public Deal proposeCounterPrice(Long dealId, double counterPrice, String sellerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getProduct().getUser().getUsername().equalsIgnoreCase(sellerUsername)) {
            throw new RuntimeException("You are not authorized to propose a counter price");
        }

        deal.setCounterPrice(counterPrice);
        deal.setStatus(DealStatus.PENDING);
        return dealRepository.save(deal);
    }
    public Deal acceptCounterPrice(Long dealId, String buyerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getBuyer().getUsername().equalsIgnoreCase(buyerUsername)) {
            throw new RuntimeException("You are not authorized to accept this deal");
        }

        if (deal.getCounterPrice() == null) {
            throw new RuntimeException("No counter price to accept");
        }

        deal.setProposedPrice(deal.getCounterPrice());
        deal.setCounterPrice(null);
        deal.setStatus(DealStatus.ACCEPTED);
        return dealRepository.save(deal);
    }


}