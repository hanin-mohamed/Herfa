package com.ProjectGraduation.aucation.repository;

import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    List<AuctionItem> findByActiveTrue();
    List<AuctionItem> findByEndTimeBeforeAndActiveTrue(LocalDateTime time);
    List<AuctionItem> findAllByCreatedByUsername(String username);

}

