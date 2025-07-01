package com.ProjectGraduation.notification.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.event.entity.Event;
import com.ProjectGraduation.event.repo.EventRepo;
import com.ProjectGraduation.notification.entity.NotificationMessage;
import com.ProjectGraduation.notification.entity.NotificationSendResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final FirebaseMessagingService firebaseMessagingService;
    private final UserRepository userRepository;
    private final EventRepo repo ;
    private final EventRepo eventRepo;


    public String sendToAllUser(NotificationMessage notificationMessage){
        List<User> users = userRepository.findAll();
        NotificationSendResult result = firebaseMessagingService.sendToUsers(users, notificationMessage);
        firebaseMessagingService.persistNotification(notificationMessage);
        return firebaseMessagingService.formatResultMessage(result, users.size(), "all users");
    }
    public String sentToUserByUserName(String username , NotificationMessage notificationMessage){
        return firebaseMessagingService.sendNotification(username, notificationMessage);
    }

    public String sendToInterestedUsers(Long eventId, NotificationMessage notificationMessage) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<User> interestedUsers = event.getInterestedUsers();
        NotificationSendResult result = firebaseMessagingService.sendToUsers(interestedUsers, notificationMessage);
        firebaseMessagingService.persistNotification(notificationMessage);
        return firebaseMessagingService.formatResultMessage(result, interestedUsers.size(), "interested users");
    }


}
