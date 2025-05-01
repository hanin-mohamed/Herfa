package com.ProjectGraduation.order.repository;

import com.ProjectGraduation.order.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailsRepo extends JpaRepository<OrderDetails , Long> {
}
