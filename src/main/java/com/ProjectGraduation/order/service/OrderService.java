package com.ProjectGraduation.order.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.offers.autoOffers.service.AutoOfferService;
import com.ProjectGraduation.offers.coupons.entity.Coupon;
import com.ProjectGraduation.offers.coupons.service.CouponService;
import com.ProjectGraduation.offers.productoffer.service.ProductOfferService;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.dto.OrderItemRequest;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.ProjectGraduation.order.repository.OrderRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final ProductService productService;
    private final UserRepo userRepo;
    private final CouponService couponService;
    private final ProductOfferService productOfferService;
    private final AutoOfferService autoOfferService;

    @Transactional
    public void createOrder(String username, OrderRequest orderRequest) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Long> productIds = orderRequest.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productMap = productService.getProductsMapByIds(productIds);

        Order order = new Order();
        order.setUser(user);
        double totalPrice = 0.0;

        for (OrderItemRequest item : orderRequest.getItems()) {
            Product product = productMap.get(item.getProductId());
            validateProductAvailability(product, item.getQuantity());

            product.setQuantity(product.getQuantity() - item.getQuantity());

            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantity(item.getQuantity());

            double itemPrice = calculateFinalItemPrice(product, item.getQuantity(), item.getCouponCode(), orderDetails);
            totalPrice += itemPrice;

            order.getOrderDetails().add(orderDetails);
        }

        order.setTotalPrice(Math.max(totalPrice, 0));
        orderRepo.save(order);
    }

    private void validateProductAvailability(Product product, int quantity) {
        if (product == null) {
            throw new IllegalStateException("Product not found.");
        }
        if (product.getQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock for product: " + product.getName());
        }
    }

    private double calculateFinalItemPrice(Product product, int quantity, String couponCode, OrderDetails orderDetails) {
        double unitPrice = product.getDiscountedPrice();
        orderDetails.setUnitPrice(unitPrice);

        double itemPrice = unitPrice * quantity;

        if (couponCode != null && !couponCode.isEmpty()) {
            double discount = couponService.applyCouponToProduct(product, quantity, couponCode);
            itemPrice -= discount;
            itemPrice = Math.max(itemPrice, 0);
            couponService.confirmCouponUsage(couponCode);
            Coupon coupon = couponService.getCouponByCode(couponCode);
            orderDetails.setCoupon(coupon);
        }

        return itemPrice;
    }

    public List<Order> getOrdersByUsername(String username) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        return orderRepo.findByUser(user);
    }

    public Order getOrderById(Long orderId, String username) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getUsername().equalsIgnoreCase(username)) {
            throw new RuntimeException("You are not authorized to view this order");
        }

        return order;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        for (OrderDetails details : order.getOrderDetails()) {
            Product product = details.getProduct();
            product.setQuantity(product.getQuantity() + details.getQuantity());
            productService.saveProduct(product);
        }
        orderRepo.delete(order);
    }
}
