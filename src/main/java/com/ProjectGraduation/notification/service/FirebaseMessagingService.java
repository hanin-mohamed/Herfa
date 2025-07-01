package com.ProjectGraduation.notification.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.notification.entity.NotificationMessage;
import com.ProjectGraduation.notification.entity.NotificationSendResult;
import com.ProjectGraduation.notification.exception.NotificationException;
import com.ProjectGraduation.notification.repo.NotificationRepo;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@AllArgsConstructor
@Service
public class FirebaseMessagingService {
    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository ;
    private final NotificationRepo notificationRepo;

    public void  validateFcmToken(User user) {
        if (user.getFCMToken() == null || user.getFCMToken().isEmpty()) {
            throw new IllegalArgumentException("User "+user.getUsername() + " does not have a valid FCM token.");
        }
    }
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    public String sendFcmNotification(String fcmToken, NotificationMessage notificationMessage) throws FirebaseMessagingException {

        Notification notification = Notification.builder()
                .setTitle(notificationMessage.getTitle())
                .setBody(notificationMessage.getBody())
                .setImage(notificationMessage.getImage())
                .build();

        Message message = Message.builder()
                .setNotification(notification)
                .putAllData(notificationMessage.getData())
                .setToken(fcmToken)
                .build();
        return firebaseMessaging.send(message) ;
    }
    public void persistNotification( NotificationMessage notificationMessage) {
        try {
            notificationRepo.save(notificationMessage);
        }catch (Exception e) {
            throw new RuntimeException("Failed to persist notification: " + e.getMessage());
        }

    }

    public String formatResultMessage (NotificationSendResult result , int total , String target){
        return String.format("Sent to %s: %d success, %d skipped, %d total",
                target,result.getSkippedCount(),result.getSkippedCount(),total
        );
    }

    public NotificationSendResult sendToUsers(List<User> users , NotificationMessage notificationMessage) {
        int successCount = 0;
        int skippedCount = 0;
        for (User user : users) {
            if (user.getFCMToken() == null) {
                skippedCount++;
                continue;
            }
            try {
                String messageId = sendFcmNotification(user.getFCMToken(), notificationMessage);
                successCount++ ;
            }
            catch (FirebaseMessagingException e) {
                System.err.println("Failed to send notification to user " + user.getUsername() + ": " + e.getMessage());
            }
        }
        return new NotificationSendResult(successCount, skippedCount);
    }



    public String sendAndPersistNotification(User user , NotificationMessage notificationMessage) {
        try {
            String messageId = sendFcmNotification(user.getFCMToken(),  notificationMessage);
            persistNotification(notificationMessage);
            return "Notification sent successfully: " + messageId;
        }
        catch (FirebaseMessagingException e) {
            throw new NotificationException("Failed to send notification to " + user.getUsername() + ": " + e.getMessage(), e);
        }
    }
    public String sendNotification(String username ,NotificationMessage notificationMessage) {
        User user = getUserByUsername(username);
        validateFcmToken(user);
        return sendAndPersistNotification(user , notificationMessage);
    }

}
