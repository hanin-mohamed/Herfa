package com.ProjectGraduation.order.payoutOrder;

import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;
import com.ProjectGraduation.order.service.OrderService;
import com.ProjectGraduation.order.utils.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPayoutScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
    public void processPayouts() {
        Date threshold = new Date(System.currentTimeMillis() - 24*60*60*1000);
        List<Order> eligibleOrders = orderRepository.findPaidOrdersOlderThan24HoursWithoutApprovedRefund(threshold);
        for (Order order : eligibleOrders) {
            orderService.confirmOrderAndDistributeFunds(order);
            order.setStatus(OrderStatus.COMPLETED);
        }
    }
}
