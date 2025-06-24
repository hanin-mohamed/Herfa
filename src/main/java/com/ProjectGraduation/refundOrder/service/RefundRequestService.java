package com.ProjectGraduation.refundOrder.service;


import com.ProjectGraduation.appWallet.service.AppWalletService;
import com.ProjectGraduation.file.CloudinaryService;
import com.ProjectGraduation.refundOrder.entity.RefundReasonType;
import com.ProjectGraduation.refundOrder.entity.RefundRequest;
import com.ProjectGraduation.refundOrder.entity.RefundStatus;
import com.ProjectGraduation.refundOrder.repository.RefundRequestRepository;
import com.ProjectGraduation.transaction.service.TransactionHistoryService;
import org.springframework.stereotype.Service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;
import com.ProjectGraduation.order.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundRequestService {

        private final RefundRequestRepository refundRepo;
        private final OrderRepository orderRepo;
        private final AppWalletService appWalletService;
        private final CloudinaryService cloudinaryService;
        private final TransactionHistoryService transactionHistoryService;

        public RefundRequest createRefundRequest(User user, Long orderId, RefundReasonType reasonType, String message, List<MultipartFile> images) {
            Order order = orderRepo.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("You can only request refund for your own orders.");
            }

            // Check if images are required
            if ((reasonType == RefundReasonType.DAMAGED || reasonType == RefundReasonType.NOT_AS_DESCRIBED)
                    && (images == null || images.isEmpty())) {
                throw new RuntimeException("Images are required for this type of refund request.");
            }

            // Upload images to Cloudinary and get URLs
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                imageUrls = uploadImagesToCloudinary(images);
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

        private List<String> uploadImagesToCloudinary(List<MultipartFile> images) {
            List<String> imageUrls = new ArrayList<>();

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String contentType = image.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        throw new RuntimeException("Only image files are allowed");
                    }

                    try {
                        String imageUrl = cloudinaryService.uploadImage(image, "refund-requests", 0L);
                        imageUrls.add(imageUrl);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage());
                    }
                }
            }

            return imageUrls;
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

            User user = request.getUser();
            user.setWalletBalance(user.getWalletBalance() + refundAmount);

            appWalletService.deductFromAppForRefund(refundAmount);
            user.setWalletBalance(user.getWalletBalance() + refundAmount);
            appWalletService.deductFromAppForRefund(refundAmount);

            transactionHistoryService.recordTransaction(
                    user, request.getOrder().getId(), "REFUND", refundAmount, user.getWalletBalance(),
                    "Refund approved"
            );
            transactionHistoryService.recordTransaction(
                    null, request.getOrder().getId(), "APP_REFUND", -refundAmount, appWalletService.getWallet().getAppBalance(),
                    "App paid refund to user"
            );


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
            request.setReasonMessage(request.getReasonMessage() + "\n[MERCHANT Note]: " + reason);

            return refundRepo.save(request);
        }
    }
