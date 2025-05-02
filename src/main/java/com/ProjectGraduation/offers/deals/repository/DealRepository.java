package com.ProjectGraduation.offers.deals.repository;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.deals.entity.Deal;
import com.ProjectGraduation.offers.deals.utils.DealStatus;
import com.ProjectGraduation.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, Long> {


    List<Deal> findByBuyer(User buyer);
    List<Deal> findByProduct_User(User seller);
    boolean existsByBuyerAndProductAndStatus(User buyer, Product product, DealStatus status);

}
