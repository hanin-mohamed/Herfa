package com.ProjectGraduation.order.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.order.dto.OrderRequest;
import com.ProjectGraduation.order.dto.OrderItemRequest;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.entity.OrderDetails;
import com.ProjectGraduation.order.repo.OrderRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.service.ProductService;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
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

    @Transactional
    public void createOrder(String username, OrderRequest orderRequest) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Long> productIds = orderRequest.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productService.getProductsByIds(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Order order = new Order();
        order.setUser(user);

        double totalPrice = 0.0;

        for (OrderItemRequest item : orderRequest.getItems()) {
            Product product = productMap.get(item.getProductId());

            if (product == null) {
                throw new IllegalStateException("Product not found with ID: " + item.getProductId());
            }

            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Not enough stock for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());

            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setOrder(order);
            orderDetails.setProduct(product);
            orderDetails.setQuantity(item.getQuantity());
            orderDetails.setUnitPrice(product.getPrice());

            order.getOrderDetails().add(orderDetails);

            totalPrice += product.getPrice() * item.getQuantity();
        }

        order.setTotalPrice(totalPrice);

        orderRepo.save(order);
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
