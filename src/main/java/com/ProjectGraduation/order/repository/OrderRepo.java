package com.ProjectGraduation.order.repository;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order , Long> {
    List<Order> findByUser(User user);
    Integer countByUser(User user);
}
