package com.ProjectGraduation.refundOrder.entity;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.order.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "refund_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Order order;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private RefundReasonType reasonType;

    @Column(length = 1000)
    private String reasonMessage;

    @ElementCollection
    @CollectionTable(name = "refund_images", joinColumns = @JoinColumn(name = "refund_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;


    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
}
