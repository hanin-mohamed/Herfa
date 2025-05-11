package com.ProjectGraduation.aucation.controller;

import com.ProjectGraduation.aucation.dto.BidRequestDTO;
import com.ProjectGraduation.aucation.dto.BidResponseDTO;
import com.ProjectGraduation.aucation.service.BidService;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuctionBidSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final BidService bidService;
    private final UserRepository userRepo;

    @MessageMapping("/bid.send.{auctionId}")
    public void handleBid(@DestinationVariable Long auctionId,
                          @Payload BidRequestDTO bidRequest,
                          SimpMessageHeaderAccessor headerAccessor) {

        try {
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            if (username == null) {
                messagingTemplate.convertAndSend("/topic/bid/" + auctionId,
                        BidResponseDTO.failed("Unauthorized: Invalid session", auctionId));
                return;
            }

            User user = userRepo.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            BidResponseDTO response = bidService.makeBid(user, auctionId, bidRequest.getAmount());

            // Broadcast result to everyone watching this auction
            messagingTemplate.convertAndSend("/topic/bid/" + auctionId, response);

        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/bid/" + auctionId,
                    BidResponseDTO.failed("Failed to place bid: " + e.getMessage(), auctionId));
        }
    }
}
