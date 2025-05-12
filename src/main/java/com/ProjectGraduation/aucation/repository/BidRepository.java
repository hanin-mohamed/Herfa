package com.ProjectGraduation.aucation.repository;

import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.aucation.entity.Bid;
import com.ProjectGraduation.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findTopByAuctionItemOrderByBidAmountDesc(AuctionItem auctionItem);
    List<Bid> findByAuctionItemOrderByBidAmountDesc(AuctionItem auctionItem);
    List<Bid> findByAuctionItem(AuctionItem auctionItem);
    List<Bid> findByAuctionItemAndUser(AuctionItem auctionItem, User user);


}