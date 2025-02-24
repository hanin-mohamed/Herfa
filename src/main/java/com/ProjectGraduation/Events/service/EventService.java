package com.ProjectGraduation.Events.service;

import com.ProjectGraduation.Events.entity.Event;
import com.ProjectGraduation.Events.repo.EventRepo;
import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.MerchantRepo;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.product.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventService {

    @Autowired
    private EventRepo eventRepository;

    @Autowired
    private MerchantRepo merchantRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Transactional
    public Event createEvent(String token, String name, String description, MultipartFile media, LocalDateTime endTime, Double price) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        Merchant merchant = merchantRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("Merchant not found"));

        // Upload file and get the stored filename
        String mediaFileName = "";
        try {
            mediaFileName = fileService.uploadFile(path, media, merchant.getId());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload media file", e);
        }

        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setMedia(mediaFileName);
        event.setStartTime(LocalDateTime.now());  // Set current time as start time
        event.setEndTime(endTime);
        event.setPrice(price);
        event.setMerchant(merchant);

        return eventRepository.save(event);
    }

    @Transactional
    public void expressInterest(Long eventId, String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        if (!event.getInterestedUsers().contains(user)) {
            event.addInterestedUser(user);
            eventRepository.save(event);
        } else {
            throw new IllegalStateException("User already interested in this event");
        }
    }

    @Transactional
    public void removeInterest(Long eventId, String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        if (event.getInterestedUsers().contains(user)) {
            event.removeInterestedUser(user);
            eventRepository.save(event);
        } else {
            throw new IllegalStateException("User is not interested in this event");
        }
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}