package com.ProjectGraduation.aucation.service;

import com.ProjectGraduation.aucation.dto.BidResponseDTO;
import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.aucation.entity.Bid;
import com.ProjectGraduation.aucation.repository.AuctionItemRepository;
import com.ProjectGraduation.aucation.repository.BidRepository;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BidService {

    private final BidRepository bidRepo;
    private final AuctionItemRepository auctionRepo;
    private final UserRepository userRepo;

    public BidResponseDTO makeBid(User user, Long auctionId, double amount) {
        AuctionItem auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!auction.isActive() || auction.getEndTime().isBefore(LocalDateTime.now())) {
            return BidResponseDTO.failed("Auction is closed", auctionId);
        }

        if (auction.getStartTime().isAfter(LocalDateTime.now())) {
            return BidResponseDTO.failed("Auction hasn't started yet", auctionId);
        }

        if (amount <= auction.getCurrentBid()) {
            return BidResponseDTO.failed("Bid must be higher than current", auctionId);
        }

        if (amount < auction.getStartingBid() * 1.05) { // Require at least 5% higher than starting bid
            return BidResponseDTO.failed("Bid must be at least 5% higher than starting bid", auctionId);
        }

        if (user.getWalletBalance() < amount) {
            return BidResponseDTO.failed("Insufficient balance", auctionId);
        }

        user.setWalletBalance(user.getWalletBalance() - amount);
        user.setReservedBalance(user.getReservedBalance() + amount);
        userRepo.save(user);

        auction.setCurrentBid(amount);
        auction.setHighestBidder(user);
        auctionRepo.save(auction);

        Bid newBid = new Bid();
        newBid.setAuctionItem(auction);
        newBid.setBidAmount(amount);
        newBid.setUser(user);
        newBid.setBidTime(LocalDateTime.now());
        bidRepo.save(newBid);

        return BidResponseDTO.builder()
                .success(true)
                .message("Bid placed successfully")
                .auctionId(auctionId)
                .currentBid(amount)
                .highestBidder(user.getUsername())
                .build();
    }
    public List<BidResponseDTO> getBidsForAuction(Long auctionId) {
        AuctionItem auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        return bidRepo.findByAuctionItemOrderByBidAmountDesc(auction)
                .stream()
                .map(bid -> BidResponseDTO.builder()
                        .auctionId(auctionId)
                        .currentBid(bid.getBidAmount())
                        .highestBidder(bid.getUser().getUsername())
                        .success(true)
                        .message("Bid record")
                        .build())
                .toList();
    }

}