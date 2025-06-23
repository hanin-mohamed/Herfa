package com.ProjectGraduation.refundOrder.service;


import com.ProjectGraduation.appWallet.service.AppWalletService;
import com.ProjectGraduation.refundOrder.entity.RefundReasonType;
import com.ProjectGraduation.refundOrder.entity.RefundRequest;
import com.ProjectGraduation.refundOrder.entity.RefundStatus;
import com.ProjectGraduation.refundOrder.repository.RefundRequestRepository;
import org.springframework.stereotype.Service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundRequestService {

    private final RefundRequestRepository refundRepo;
    private final OrderRepository orderRepo;
    private final AppWalletService appWalletService;

    public RefundRequest createRefundRequest(User user, Long orderId, RefundReasonType reasonType, String message, List<String> imageUrls) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only request refund for your own orders.");
        }

        // Check if image is required
        if ((reasonType == RefundReasonType.DAMAGED || reasonType == RefundReasonType.NOT_AS_DESCRIBED)
                && (imageUrls == null || imageUrls.isEmpty())) {
            throw new RuntimeException("Images are required for this type of refund request.");
        }

        RefundRequest request = new RefundRequest();
        request.setUser(user);
        request.setOrder(order);
        request.setReasonType(reasonType);
        request.setReasonMessage(message);
        request.setImageUrls(imageUrls);
        request.setStatus(RefundStatus.PENDING);

        return refundRepo.save(request);
    }

    public List<RefundRequest> getAllRequestsForAdmin() {
        return refundRepo.findAll();
    }

    public List<RefundRequest> getRequestsForUser(User user) {
        return refundRepo.findByUser(user);
    }

    @Transactional
    public RefundRequest approveRefund(Long requestId) {
        RefundRequest request = refundRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Refund request not found"));

        if (request.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Refund already handled.");
        }

        double refundAmount = request.getOrder().getTotalPrice();

        // Step 1: update wallet balance of user
        User user = request.getUser();
        user.setWalletBalance(user.getWalletBalance() + refundAmount);

        // Step 2: update app wallet balance
        appWalletService.deductFromAppForRefund(refundAmount);

        request.setStatus(RefundStatus.APPROVED);
        return refundRepo.save(request);
    }

    public RefundRequest rejectRefund(Long requestId, String reason) {
        RefundRequest request = refundRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Refund request not found"));

        if (request.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Refund already handled.");
        }

        request.setStatus(RefundStatus.REJECTED);
        request.setReasonMessage(request.getReasonMessage() + "\n[Admin Note]: " + reason);

        return refundRepo.save(request);
    }
}
