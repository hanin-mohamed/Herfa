package com.ProjectGraduation.aucation.repository;

import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.aucation.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByAuctionItemOrderByBidAmountDesc(AuctionItem auctionItem);
    Optional<Bid> findByAuctionItemOrderByBidAmountDesc(AuctionItem auctionItem);
}