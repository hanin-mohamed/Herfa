package com.ProjectGraduation.aucation.service;

import com.ProjectGraduation.aucation.dto.AuctionResponseDTO;
import com.ProjectGraduation.aucation.dto.BidResponseDTO;
import com.ProjectGraduation.aucation.entity.AuctionItem;
import com.ProjectGraduation.aucation.entity.Bid;
import com.ProjectGraduation.aucation.repository.AuctionItemRepository;
import com.ProjectGraduation.aucation.repository.BidRepository;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.product.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionItemRepository auctionRepo;
    private final FileService fileService;
    private final BidRepository bidRepo;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${project.poster}")
    private String uploadPath;

    public AuctionResponseDTO createAuction(User user, MultipartFile image, String title,
                                            String description, double startingBid,
                                            LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past.");
        }
        if (image.isEmpty()) {
            throw new IllegalArgumentException("Image cannot be empty.");
        }

        String fileName = fileService.uploadFile(uploadPath, image, user.getId(), "auction", title);

        AuctionItem auction = new AuctionItem();
        auction.setTitle(title);
        auction.setDescription(description);
        auction.setStartingBid(startingBid);
        auction.setCurrentBid(startingBid);
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        auction.setCreatedBy(user);
        auction.setImageUrl(fileName);

        AuctionItem saved = auctionRepo.save(auction);
        return toDTO(saved);
    }

    public void endAuction(Long auctionId) {
        AuctionItem auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (auction.getEndTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Auction has not ended yet");
        }
        if (!auction.isActive()) {
            throw new RuntimeException("Auction is already ended");
        }

        Optional<Bid> topBid = bidRepo.findTopByAuctionItemOrderByBidAmountDesc(auction);

        topBid.ifPresent(bid -> {
            User winner = bid.getUser();
            double bidAmount = bid.getBidAmount();

            if (winner.getReservedBalance() < bidAmount) {
                throw new RuntimeException("Reserved balance inconsistency for winner.");
            }

            winner.setWalletBalance(winner.getWalletBalance() - bidAmount);
            winner.setReservedBalance(winner.getReservedBalance() - bidAmount);

            auction.setWinner(winner);
            userRepository.save(winner);

            // Notify winner
            messagingTemplate.convertAndSendToUser(
                    winner.getUsername(),
                    "/queue/auction-result",
                    BidResponseDTO.builder()
                            .success(true)
                            .message("Congratulations! You won auction #" + auctionId)
                            .auctionId(auctionId)
                            .currentBid(bidAmount)
                            .highestBidder(winner.getUsername())
                            .build()
            );
        });

        auction.setActive(false);
        auctionRepo.save(auction);

        // Notify all clients that the auction has ended
        messagingTemplate.convertAndSend("/topic/bid/" + auctionId,
                BidResponseDTO.builder()
                        .success(false)
                        .message("Auction has ended")
                        .auctionId(auctionId)
                        .highestBidder(topBid.map(bid -> bid.getUser().getUsername()).orElse(null))
                        .currentBid(auction.getCurrentBid())
                        .build());
    }

    public void finalizeExpiredAuctions() {
        List<AuctionItem> expiredAuctions = auctionRepo.findByEndTimeBeforeAndActiveTrue(LocalDateTime.now());
        for (AuctionItem auction : expiredAuctions) {
            try {
                endAuction(auction.getId());
            } catch (Exception e) {
                System.out.println("Failed to finalize auction " + auction.getId() + ": " + e.getMessage());
            }
        }
    }

//    @Scheduled(fixedRate = 60000)
    public void autoFinalizeAuctions() {
        finalizeExpiredAuctions();
    }

    private AuctionResponseDTO toDTO(AuctionItem auction) {
        return AuctionResponseDTO.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .imageUrl(auction.getImageUrl())
                .startingBid(auction.getStartingBid())
                .currentBid(auction.getCurrentBid())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .active(auction.isActive())
                .createdByUsername(auction.getCreatedBy().getUsername())
                .createdByWallet(auction.getCreatedBy().getWalletBalance())
                .highestBidder(auction.getHighestBidder() != null ? auction.getHighestBidder().getUsername() : null)
                .build();
    }
    public List<AuctionResponseDTO> getAllAuctions() {
        return auctionRepo.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public AuctionResponseDTO getAuctionById(Long id) {
        AuctionItem auction = auctionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        return toDTO(auction);
    }

    public List<AuctionResponseDTO> getAuctionsByMerchant(String username) {
        return auctionRepo.findAllByCreatedByUsername(username)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AuctionResponseDTO> getActiveAuctions() {
        return auctionRepo.findByActiveTrue().stream()
                .map(this::toDTO)
                .toList();
    }
}