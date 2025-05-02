package com.ProjectGraduation.order.repository;

import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.utils.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.user.username = :username")
    Optional<Order> findByIdAndUserUsername(@Param("orderId") Long orderId, @Param("username") String username);

    @Query("SELECT o FROM Order o WHERE o.user.username = :username")
    List<Order> findByUserUsername(@Param("username") String username);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user")
    long countByUser(@Param("user") com.ProjectGraduation.auth.entity.User user);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);
}