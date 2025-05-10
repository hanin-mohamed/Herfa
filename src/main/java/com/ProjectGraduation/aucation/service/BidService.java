package com.ProjectGraduation.aucation.service;


import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.aucation.entity.Bid;
import com.ProjectGraduation.aucation.repository.AuctionItemRepository;
import com.ProjectGraduation.aucation.repository.BidRepository;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final AuctionItemRepository auctionRepo;
    private final BidRepository bidRepo;
    private final UserRepository userRepo;

    public Bid makeBid(Long auctionId, User bidder, double bidAmount) {
        AuctionItem auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (LocalDateTime.now().isBefore(auction.getStartTime())) {
            throw new RuntimeException("Auction has not started yet");
        }
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new RuntimeException("Auction has ended");
        }

        Optional<Bid> currentHighest = bidRepo.findTopByAuctionItemOrderByBidAmountDesc(auction);
        double current = currentHighest.map(Bid::getBidAmount).orElse(auction.getStartingBid());

        // Check if the bid amount is higher than the current highest bid and if the bidder has enough balance
        if (bidAmount <= current) {
            throw new RuntimeException("Bid must be higher than current bid");
        }
        if ((bidder.getWalletBalance() - bidder.getReservedBalance()) < bidAmount) {
            throw new RuntimeException("Insufficient available balance");
        }

        // refund the previous highest bidder if there is one
        currentHighest.ifPresent(prevBid->{
            User previousBidder = prevBid.getUser();
            previousBidder.setReservedBalance(previousBidder.getReservedBalance() - prevBid.getBidAmount());
            userRepo.save(previousBidder);});


        // Reserve the bid amount
        bidder.setReservedBalance(bidder.getReservedBalance() + bidAmount);
        userRepo.save(bidder);

       // Save the new bid
        Bid bid = new Bid();
        bid.setBidAmount(bidAmount);
        bid.setUser(bidder);
        bid.setAuctionItem(auction);
        bid.setBidTime(LocalDateTime.now());
        bidRepo.save(bid);

        // Update the auction's current bid
        auction.setCurrentBid(bidAmount);
        auctionRepo.save(auction);
        return bid;
    }
}
