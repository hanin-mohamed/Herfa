package com.ProjectGraduation.order.service;

import java.time.LocalDateTime;
import java.util.List;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.ProjectGraduation.order.repo.OrderDetailsRepo;
import com.ProjectGraduation.order.repo.OrderRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderDetailsRepo detailsRepo;

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public Order createOrder(Long userId, List<OrderDetails> productOrders, double userBudget) throws Exception {

        if (productOrders == null || productOrders.isEmpty()) {
            throw new IllegalArgumentException("Product list cannot be empty.");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now().toString());
        order.setStatus("PENDING");
        order.setUser(user);

        double totalPrice = 0.0;

        for (OrderDetails orderDetails : productOrders) {

            if (orderDetails.getProduct() == null || orderDetails.getProduct().getId() == null) {
                throw new IllegalArgumentException("Each order must have a valid product ID.");
            }

            Product product = productRepo.findById(orderDetails.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found"));

            if (product.getQuantity() < orderDetails.getQuantity()) {
                throw new IllegalStateException("Not enough stock for product ID: " + product.getId());
            }

            double productTotal = product.getPrice() * orderDetails.getQuantity();
            totalPrice += productTotal;

            product.setQuantity(product.getQuantity() - orderDetails.getQuantity());
            productRepo.save(product);

            OrderDetails orderDetails1 = new OrderDetails();
            orderDetails1.setOrder(order);
            orderDetails1.setProduct(product);
            orderDetails1.setQuantity(orderDetails.getQuantity());
            orderDetails1.setUnitPrice(productTotal);

            order.getOrderDetails().add(orderDetails1);
        }

        if (userBudget < totalPrice) {
            throw new IllegalArgumentException("Insufficient funds! Total price is " + totalPrice + ", but you provided " + userBudget);
        }

        order.setTotalPrice(totalPrice);

        order = orderRepo.save(order);
        detailsRepo.saveAll(order.getOrderDetails());

        return order;
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new RuntimeException("Order Not Found");
        }
        orderRepo.deleteById(orderId);
    }
}
