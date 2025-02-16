package com.ProjectGraduation.order.repo;

import com.ProjectGraduation.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order , Long> {
}
