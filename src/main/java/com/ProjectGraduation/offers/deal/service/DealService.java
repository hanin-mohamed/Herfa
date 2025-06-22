package com.ProjectGraduation.offers.deal.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.offers.deal.dto.DealRequest;
import com.ProjectGraduation.offers.deal.dto.DealResponse;
import com.ProjectGraduation.offers.deal.entity.Deal;
import com.ProjectGraduation.offers.deal.repository.DealRepository;
import com.ProjectGraduation.offers.deal.utils.DealStatus;
import com.ProjectGraduation.order.service.OrderService;
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
    private final OrderService orderService;
    @Transactional
    public DealResponse createDeal(DealRequest request, String username) {
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
        deal.setCounterQuantity(null);
        deal.setStatus(DealStatus.PENDING);

        return mapToDealResponse(dealRepository.save(deal));
    }

    public List<DealResponse> getDealsForBuyer(String username) {
        User buyer = userService.getUserByUsername(username);
        return dealRepository.findByBuyer(buyer)
                .stream()
                .map(this::mapToDealResponse)
                .toList();
    }

    public List<DealResponse> getDealsForSeller(String username) {
        User seller = userService.getUserByUsername(username);
        return dealRepository.findByProduct_User(seller)
                .stream()
                .map(this::mapToDealResponse)
                .toList();
    }

    public DealResponse updateDealStatus(Long dealId, DealStatus status, String sellerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getProduct().getUser().getUsername().equalsIgnoreCase(sellerUsername)) {
            throw new RuntimeException("You are not authorized to update this deal");
        }

        deal.setStatus(status);
        return mapToDealResponse(dealRepository.save(deal));
    }

    public DealResponse proposeCounterOffer(Long dealId, double counterPrice, int counterQuantity, String sellerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getProduct().getUser().getUsername().equalsIgnoreCase(sellerUsername)) {
            throw new RuntimeException("You are not authorized to propose a counter offer");
        }

        deal.setCounterPrice(counterPrice);
        deal.setCounterQuantity(counterQuantity);
        deal.setStatus(DealStatus.COUNTERED);
        return mapToDealResponse(dealRepository.save(deal));
    }

    public DealResponse acceptCounterOffer(Long dealId, String buyerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getBuyer().getUsername().equalsIgnoreCase(buyerUsername)) {
            throw new RuntimeException("You are not authorized to accept this deal");
        }

        if (deal.getCounterPrice() == null || deal.getCounterQuantity() == null) {
            throw new RuntimeException("No counter offer to accept");
        }

        deal.setProposedPrice(deal.getCounterPrice());
        deal.setRequestedQuantity(deal.getCounterQuantity());
        deal.setCounterPrice(null);
        deal.setCounterQuantity(null);
        deal.setStatus(DealStatus.ACCEPTED);
        // Create an order based on the accepted deal
        orderService.createOrderFromDeal(deal);
        return mapToDealResponse(dealRepository.save(deal));
    }
    public DealResponse sellerAccept(Long dealId, String sellerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getProduct().getUser().getUsername().equalsIgnoreCase(sellerUsername)) {
            throw new RuntimeException("Not your product");
        }

        if (deal.getStatus() != DealStatus.PENDING) {
            throw new RuntimeException("Only pending deals can be accepted");
        }

        deal.setStatus(DealStatus.ACCEPTED);
        // Create an order based on the accepted deal
        orderService.createOrderFromDeal(deal);
        return mapToDealResponse(dealRepository.save(deal));
    }

    public DealResponse sellerReject(Long dealId, String sellerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getProduct().getUser().getUsername().equalsIgnoreCase(sellerUsername)) {
            throw new RuntimeException("Not your product");
        }

        if (deal.getStatus() != DealStatus.PENDING) {
            throw new RuntimeException("Only pending deals can be rejected");
        }

        deal.setStatus(DealStatus.REJECTED);
        return mapToDealResponse(dealRepository.save(deal));
    }

    public DealResponse buyerRejectCounter(Long dealId, String buyerUsername) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (!deal.getBuyer().getUsername().equalsIgnoreCase(buyerUsername)) {
            throw new RuntimeException("Not your deal");
        }

        if (deal.getStatus() != DealStatus.COUNTERED) {
            throw new RuntimeException("No counter to reject");
        }

        deal.setStatus(DealStatus.REJECTED);
        return mapToDealResponse(dealRepository.save(deal));
    }

    private DealResponse mapToDealResponse(Deal deal) {
        return DealResponse.builder()
                .id(deal.getId())
                .buyerUsername(deal.getBuyer().getUsername())
                .product(deal.getProduct())
                .requestedQuantity(deal.getRequestedQuantity())
                .proposedPrice(deal.getProposedPrice())
                .counterPrice(deal.getCounterPrice())
                .counterQuantity(deal.getCounterQuantity())
                .status(deal.getStatus())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
    }
}
