package com.ProjectGraduation.order.repo;

import com.ProjectGraduation.order.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailsRepo extends JpaRepository<OrderDetails , Long> {
}
