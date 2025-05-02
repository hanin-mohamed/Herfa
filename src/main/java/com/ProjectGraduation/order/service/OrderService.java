package com.ProjectGraduation.order.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.offers.autoOffers.service.AutoOfferService;
import com.ProjectGraduation.offers.autoOffers.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffers.utils.AutoOfferType;
import com.ProjectGraduation.offers.coupons.service.CouponService;
import com.ProjectGraduation.offers.coupons.entity.Coupon;
import com.ProjectGraduation.offers.productoffer.service.ProductOfferService;
import com.ProjectGraduation.offers.productoffer.entity.ProductOffer;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.dto.OrderItemRequest;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.ProjectGraduation.order.helper.CouponUsageRecord;
import com.ProjectGraduation.order.helper.OfferApplication;
import com.ProjectGraduation.order.helper.ProductOfferUsageRecord;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductService productService;
    private final UserRepo userRepo;
    private final ProductOfferService productOfferService;
    private final CouponService couponService;
    private final AutoOfferService autoOfferService;

    @Transactional
    public Order createOrder(String username, OrderRequest req) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Long> ids = req.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> map = productService.getProductsByIds(ids)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDetails(new ArrayList<>());

        order = orderRepo.save(order);

        double total = 0.0;
        List<String> appliedOffers = new ArrayList<>();
        List<CouponUsageRecord> couponUsages = new ArrayList<>();
        List<ProductOfferUsageRecord> productOfferUsages = new ArrayList<>();

        for (OrderItemRequest item : req.getItems()) {
            Product product = map.get(item.getProductId());
            if (product == null || product.getQuantity() < item.getQuantity())
                throw new IllegalStateException("Invalid product or stock");

            product.setQuantity(product.getQuantity() - item.getQuantity());
            productService.saveProduct(product);

            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantity(item.getQuantity());

            double price = applyAllOffers(product, item.getQuantity(), item.getCouponCode(), user, order, appliedOffers, couponUsages, productOfferUsages);
            total += price;
            order.getOrderDetails().add(orderDetails);
        }

        order.setTotalPrice(Math.max(total, 0));
        order.setAppliedOffers(appliedOffers);
        order = orderRepo.save(order);

        for (CouponUsageRecord usage : couponUsages) {
            couponService.recordCouponUsage(usage.coupon, user, order, usage.discount);
            couponService.confirmCouponUsage(usage.couponCode);
        }

        for (ProductOfferUsageRecord usage : productOfferUsages) {
            productOfferService.recordOfferUsage(usage.offer, user, order, usage.discount);
        }

        // Add loyalty points based on total (1 point for every 10 units spent)
        int pointsEarned = (int) (total / 10);
        user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsEarned);
        userRepo.save(user);

        return order;
    }

    private double applyAllOffers(Product product, int quantity, String couponCode, User user, Order order,
                                  List<String> appliedOffers, List<CouponUsageRecord> couponUsages,
                                  List<ProductOfferUsageRecord> productOfferUsages) {
        double unitPrice = product.getPrice();
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;

        // Step 1: Apply Product Offer
        double discountedUnitPrice = applyProductOffer(product, quantity, categoryId, appliedOffers, productOfferUsages);
        product.setDiscountedPrice(discountedUnitPrice);
        double subtotal = discountedUnitPrice * quantity;

        // Step 2: Apply Coupon
        double couponDiscount = applyCoupon(product, quantity, couponCode, user, order, couponUsages, appliedOffers);
        subtotal = Math.max(subtotal - couponDiscount, 0);

        // Step 3: Apply Best Auto Offer
        double autoDiscount = applyBestAutoOffer(product, quantity, subtotal, user, order, categoryId, appliedOffers);
        subtotal = Math.max(subtotal - autoDiscount, 0);

        return subtotal;
    }

    private double applyProductOffer(Product product, int quantity, Long categoryId,
                                     List<String> appliedOffers, List<ProductOfferUsageRecord> usageRecords) {
        double unit = product.getPrice();
        Optional<ProductOffer> offer = productOfferService.getProductOffer(product.getId(), categoryId);
        if (offer.isPresent()) {
            double discounted = productOfferService.getDiscountedPrice(product);
            double discount = (unit - discounted) * quantity;
            appliedOffers.add("Product Offer: " + offer.get().getName());
            usageRecords.add(new ProductOfferUsageRecord(offer.get(), discount));
            return discounted;
        }
        return unit;
    }

    private double applyCoupon(Product product, int quantity, String couponCode, User user, Order order,
                               List<CouponUsageRecord> usageRecords, List<String> appliedOffers) {
        if (couponCode == null || couponCode.isEmpty()) return 0.0;
        try {
            double discount = couponService.applyCouponToProduct(product, quantity, couponCode);
            Coupon coupon = couponService.getCouponByCode(couponCode).orElseThrow();
            usageRecords.add(new CouponUsageRecord(coupon, couponCode, discount));
            appliedOffers.add("Coupon: " + couponCode);
            return discount;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Failed to apply coupon: " + e.getMessage());
        }
    }

    private double applyBestAutoOffer(Product product, int quantity, double subtotal, User user, Order order,
                                      Long categoryId, List<String> appliedOffers) {
        List<OfferApplication> offers = new ArrayList<>();

        autoOfferService.findFirstOrderOffer()
                .filter(offer -> orderRepo.countByUser(user) == 0 && isEligibleForAutoOffer(offer, product, quantity))
                .ifPresent(offer -> offers.add(new OfferApplication(offer, calculateAutoDiscount(offer, subtotal),
                        "First Order Offer: " + offer.getName())));

        autoOfferService.findMinOrderAmountOffer(subtotal, product.getId(), categoryId)
                .filter(offer -> isEligibleForAutoOffer(offer, product, quantity))
                .ifPresent(offer -> offers.add(new OfferApplication(offer, calculateAutoDiscount(offer, subtotal),
                        "Min Order Amount Offer: " + offer.getName())));

        autoOfferService.findLoyaltyPointsOffer(user.getLoyaltyPoints(), product.getId(), categoryId)
                .filter(offer -> isEligibleForAutoOffer(offer, product, quantity))
                .ifPresent(offer -> offers.add(new OfferApplication(offer, calculateAutoDiscount(offer, subtotal),
                        "Loyalty Points Offer: " + offer.getName())));

        autoOfferService.findBuyXGetYOffer(quantity, product.getId(), categoryId)
                .filter(offer -> isEligibleForAutoOffer(offer, product, quantity))
                .ifPresent(offer -> offers.add(new OfferApplication(offer, calculateBuyXGetYDiscount(offer, product, quantity),
                        "Buy X Get Y Offer: " + offer.getName())));

        return offers.stream()
                .max(Comparator.comparingDouble(OfferApplication::getDiscount))
                .map(offer -> {
                    appliedOffers.add(offer.getDescription());
                    AutoOffer autoOffer = (AutoOffer) offer.getOffer();
                    autoOfferService.recordOfferUsage(autoOffer, user, order, offer.getDiscount());
                    if (autoOffer.getType() == AutoOfferType.LOYALTY_POINTS && autoOffer.getRequiredPoints() != null) {
                        user.setLoyaltyPoints(user.getLoyaltyPoints() - autoOffer.getRequiredPoints());
                        userRepo.save(user);
                    }
                    return offer.getDiscount();
                })
                .orElse(0.0);
    }


    private boolean isEligibleForAutoOffer(AutoOffer offer, Product product, int quantity) {
        if (!offer.isActive()) return false;

        Date now = new Date();
        if ((offer.getStartDate() != null && now.before(offer.getStartDate())) ||
                (offer.getEndDate() != null && now.after(offer.getEndDate()))) {
            return false;
        }

        return switch (offer.getType()) {
            case FIRST_ORDER -> true;
            case MIN_ORDER_AMOUNT -> offer.getMinOrderAmount() != null;
            case LOYALTY_POINTS -> offer.getRequiredPoints() != null;
            case BUY_X_GET_Y -> offer.getBuyQuantity() != null && quantity >= offer.getBuyQuantity();
        };
    }


    private double calculateAutoDiscount(AutoOffer autoOffer, double amount) {
        if (autoOffer.getFixedPrice() != null) {
            return amount - autoOffer.getFixedPrice();
        } else {
            double disc = amount * (autoOffer.getDiscount() / 100);
            if (autoOffer.getMaxDiscount() != null && disc > autoOffer.getMaxDiscount()) {
                disc = autoOffer.getMaxDiscount();
            }
            return disc;
        }
    }

    private double calculateBuyXGetYDiscount(AutoOffer offer, Product product, int quantity) {
        int eligibleSets = quantity / offer.getBuyQuantity();
        int freeItems = eligibleSets * offer.getGetQuantity();
        double unitPrice = productOfferService.getDiscountedPrice(product);
        return freeItems * unitPrice;
    }

    @Transactional
    public Order getOrderById(Long orderId, String username) {
        return orderRepo.findByIdAndUserUsername(orderId, username)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot delete order with status " + order.getStatus());
        }
        for (OrderDetails details : order.getOrderDetails()) {
            Product product = details.getProduct();
            product.setQuantity(product.getQuantity() + details.getQuantity());
            productService.saveProduct(product);
        }
        orderRepo.delete(order);
    }

    @Transactional
    public List<Order> getOrdersByUsername(String username) {
        return orderRepo.findByUserUsername(username);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepo.save(order);
    }




}