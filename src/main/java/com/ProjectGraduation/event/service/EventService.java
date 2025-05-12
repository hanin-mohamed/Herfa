package com.ProjectGraduation.event.service;

import com.ProjectGraduation.event.entity.Event;
import com.ProjectGraduation.event.exception.*;
import com.ProjectGraduation.event.repo.EventRepo;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.product.exception.FileUploadException;
import com.ProjectGraduation.product.exception.UnauthorizedMerchantException;
import com.ProjectGraduation.product.service.FileService;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepo eventRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;



    @Transactional
    public Event createEvent(String token, String name, String description, MultipartFile media, LocalDateTime startTime, LocalDateTime endTime, Double price) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("Merchant not found with username: " + username));

        if (!user.getRole().toString().equals("MERCHANT")) {
            throw new UnauthorizedMerchantException("You are not a merchant.");
        }

        String mediaFileName;
        try {
            mediaFileName = fileService.uploadFile(path, media, user.getId(), "event", name);
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload media file: " + e.getMessage());
        }

        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setMedia(mediaFileName);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setPrice(price);
        event.setUser(user);

        return eventRepository.save(event);
    }

    @Transactional
    public void expressInterest(Long eventId, String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        if (!event.getInterestedUsers().contains(user)) {
            event.addInterestedUser(user);
            eventRepository.save(event);
        } else {
            throw new AlreadyInterestedException("User is already interested in this event.");
        }
    }

    @Transactional
    public void removeInterest(Long eventId, String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        if (event.getInterestedUsers().contains(user)) {
            event.removeInterestedUser(user);
            eventRepository.save(event);
        } else {
            throw new NotInterestedException("User is not interested in this event.");
        }
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}