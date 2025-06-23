package com.ProjectGraduation.refundOrder.repository;

import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.refundOrder.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByUser(User user);
    Optional<RefundRequest> findByOrder(Order order);
}
