package com.ProjectGraduation.offers.deal.repository;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.offers.deal.entity.Deal;
import com.ProjectGraduation.offers.deal.utils.DealStatus;
import com.ProjectGraduation.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {


    List<Deal> findByBuyer(User buyer);
    List<Deal> findByProduct_User(User seller);
    boolean existsByBuyerAndProductAndStatus(User buyer, Product product, DealStatus status);

}
