package com.ProjectGraduation.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationSendResult {

    private int successCount;
    private int skippedCount;
}
