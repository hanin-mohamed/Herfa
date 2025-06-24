package com.ProjectGraduation.order.service;

import com.ProjectGraduation.appWallet.service.AppWalletService;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.UserService;
import com.ProjectGraduation.bundle.entity.Bundle;
import com.ProjectGraduation.bundle.entity.BundleProduct;
import com.ProjectGraduation.bundle.service.BundleService;
import com.ProjectGraduation.category.entity.Category;
import com.ProjectGraduation.offers.autoOffer.entity.AutoOffer;
import com.ProjectGraduation.offers.autoOffer.service.AutoOfferService;
import com.ProjectGraduation.offers.autoOffer.utils.AutoOfferType;
import com.ProjectGraduation.offers.coupon.entity.Coupon;
import com.ProjectGraduation.offers.coupon.service.CouponService;
import com.ProjectGraduation.offers.deal.entity.Deal;
import com.ProjectGraduation.offers.deal.repository.DealRepository;
import com.ProjectGraduation.offers.productoffer.dto.AppliedOfferDTO;
import com.ProjectGraduation.offers.productoffer.entity.ProductOffer;
import com.ProjectGraduation.offers.productoffer.service.ProductOfferService;
import com.ProjectGraduation.order.dto.OrderItemDTO;
import com.ProjectGraduation.order.dto.OrderItemRequest;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.dto.OrderResponse;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.ProjectGraduation.order.helper.CouponUsageRecord;
import com.ProjectGraduation.order.helper.OfferApplication;
import com.ProjectGraduation.order.helper.ProductOfferUsageRecord;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.utils.OrderStatus;
import com.ProjectGraduation.product.dto.ProductDTO;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final ProductOfferService productOfferService;
    private final CouponService couponService;
    private final AutoOfferService autoOfferService;
    private final BundleService bundleService;
    private final DealRepository dealRepository;
    private final UserService userService;
    private final AppWalletService appWalletService;
    private final OrderRepository orderRepository;

    @Transactional
    public void confirmOrderAndDistributeFunds(Order order) {
        double totalAppFee = 0;
        Map<User, Double> sellerAmounts = new HashMap<>();

        for (OrderDetails item : order.getOrderDetails()) {
            Product product = item.getProduct();
            Category category = product.getCategory();
            double percentage = category.getPercentage() / 100.0;
            double itemTotal = item.getQuantity() * item.getUnitPrice();
            double appFee = itemTotal * percentage;
            double sellerPart = itemTotal - appFee;
            totalAppFee += appFee;
            User seller = product.getUser();
            sellerAmounts.put(seller, sellerAmounts.getOrDefault(seller, 0.0) + sellerPart);
        }

        appWalletService.releaseHeldAndAddCommission(order.getTotalPrice(), totalAppFee);

        for (Map.Entry<User, Double> entry : sellerAmounts.entrySet()) {
            userService.addToSellerWallet(entry.getKey().getId(), entry.getValue());
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Transactional
    public OrderResponse createOrder(String username, OrderRequest req) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
            if (item.getBundleId() != null) {
                Bundle bundle = bundleService.getById(item.getBundleId());
                if (!bundle.isActive()) throw new IllegalStateException("Inactive bundle");

                for (BundleProduct bp : bundle.getProducts()) {
                    Product product = bp.getProduct();
                    int requiredQty = bp.getQuantity() * item.getQuantity();
                    if (product.getQuantity() < requiredQty) {
                        throw new IllegalStateException("Insufficient stock for bundle product: " + product.getName());
                    }
                    product.setQuantity(product.getQuantity() - requiredQty);
                    productService.saveProduct(product);

                    OrderDetails details = new OrderDetails();
                    details.setOrder(order);
                    details.setProduct(product);
                    details.setQuantity(requiredQty);
                    details.setUnitPrice(0);
                    details.setCoupon(item.getCouponCode() != null ? couponService.getCouponByCode(item.getCouponCode()).orElse(null) : null);
                    order.getOrderDetails().add(details);
                }

                total += bundle.getBundlePrice() * item.getQuantity();
                appliedOffers.add("Bundle: " + bundle.getName());
            } else {
                Product product = productService.getById(item.getProductId());
                if (product == null || product.getQuantity() < item.getQuantity())
                    throw new IllegalStateException("Invalid product or stock");

                product.setQuantity(product.getQuantity() - item.getQuantity());
                productService.saveProduct(product);

                OrderDetails orderDetails = new OrderDetails();
                orderDetails.setOrder(order);
                orderDetails.setProduct(product);
                orderDetails.setQuantity(item.getQuantity());
                orderDetails.setCoupon(item.getCouponCode() != null ? couponService.getCouponByCode(item.getCouponCode()).orElse(null) : null);
                double price = applyAllOffers(product, item.getQuantity(), item.getCouponCode(), user, order, appliedOffers, couponUsages, productOfferUsages);
                orderDetails.setUnitPrice(price / item.getQuantity());

                total += price;
                order.getOrderDetails().add(orderDetails);
            }
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
        return mapToOrderResponse(order);
    }

    @Transactional
    public void createOrderFromDeal(Deal deal) {
        User buyer = deal.getBuyer();
        Product product = deal.getProduct();

        int quantity = deal.getRequestedQuantity();
        double price = deal.getProposedPrice();

        if (product.getQuantity() < quantity) {
            throw new IllegalStateException("Not enough product quantity for this deal");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productService.saveProduct(product);

        Order order = new Order();
        order.setUser(buyer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDetails(new ArrayList<>());

        OrderDetails details = new OrderDetails();
        details.setOrder(order);
        details.setProduct(product);
        details.setQuantity(quantity);
        details.setUnitPrice(price);

        order.getOrderDetails().add(details);
        order.setTotalPrice(price * quantity);
        orderRepo.save(order);
        deal.setOrder(order);
        dealRepository.save(deal);

    }

    public void confirmOrderPayment(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.PAID) return;
        order.setStatus(OrderStatus.PAID);
        User user = order.getUser();
        int pointsEarned = (int) (order.getTotalPrice() / 100);
        user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsEarned);
        userRepository.save(user);
        orderRepo.save(order);

    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDTO> items = order.getOrderDetails().stream().map(detail -> {
            Product product = detail.getProduct();
            return OrderItemDTO.builder()
                    .id(detail.getId())
                    .quantity(detail.getQuantity())
                    .unitPrice(detail.getUnitPrice())
                    .couponCode(detail.getCoupon().getCode())
                    .product(ProductDTO.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .shortDescription(product.getShortDescription())
                            .longDescription(product.getLongDescription())
                            .discountedPrice(productService.getEffectivePrice(product))
                            .price(product.getPrice())
                            .quantity(product.getQuantity())
                            .active(product.getActive())
                            .build()
                    )
                    .build();
        }).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .orderDetails(items)
                .appliedOffers(order.getAppliedOffers().stream()
                        .map(AppliedOfferDTO::new)
                        .toList())
                .build();
    }



    private double applyAllOffers(Product product, int quantity, String couponCode, User user, Order order,
                                  List<String> appliedOffers, List<CouponUsageRecord> couponUsages,
                                  List<ProductOfferUsageRecord> productOfferUsages) {

        // 1. Apply Product Offer
        double unitPrice = applyProductOffer(product, quantity, product.getCategory() != null ? product.getCategory().getId() : null,
                appliedOffers, productOfferUsages);

        // 2. Apply Coupon
        double couponDiscount = applyCoupon(product, quantity, couponCode, user, order, couponUsages, appliedOffers);
        double subtotal = Math.max(unitPrice * quantity - couponDiscount, 0);

        // 3. Apply Best Auto Offer
        double autoDiscount = applyBestAutoOffer(product, quantity, subtotal, user, order,
                product.getCategory() != null ? product.getCategory().getId() : null, appliedOffers);

        return Math.max(subtotal - autoDiscount, 0);
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
                        userRepository.save(user);
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


    @Transactional
    public void onOrderPaid(Order order) {
        appWalletService.holdAmountForSeller(order.getTotalPrice());
    }


}