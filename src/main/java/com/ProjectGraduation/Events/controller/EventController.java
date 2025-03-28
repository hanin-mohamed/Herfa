package com.ProjectGraduation.Events.controller;

import com.ProjectGraduation.Events.entity.Event;
import com.ProjectGraduation.Events.exception.AlreadyInterestedException;
import com.ProjectGraduation.Events.exception.EventNotFoundException;
import com.ProjectGraduation.Events.exception.NotInterestedException;
import com.ProjectGraduation.Events.service.EventService;
import com.ProjectGraduation.auth.api.model.ApiResponse;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.product.exception.FileUploadException;
import com.ProjectGraduation.product.exception.UnauthorizedMerchantException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> createEvent(
            @RequestHeader("Authorization") String token,
            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart("media") MultipartFile media,
            @RequestPart("startTime") String startTime,
            @RequestPart("endTime") String endTime,
            @RequestPart(value = "price", required = false) Double price) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime.trim());
            LocalDateTime end = LocalDateTime.parse(endTime.trim());
            Event event = eventService.createEvent(token, name, description, media, start, end, price);
            return ResponseEntity.ok(new ApiResponse(true, "Event created successfully", event));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (UnauthorizedMerchantException | FileUploadException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to create event: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/{eventId}/interest")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> expressInterest(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        try {
            eventService.expressInterest(eventId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Interest added successfully", null));
        } catch (UserNotFoundException | EventNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (AlreadyInterestedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to add interest: " + ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{eventId}/interest")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> removeInterest(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token) {
        try {
            eventService.removeInterest(eventId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Interest removed successfully", null));
        } catch (UserNotFoundException | EventNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (NotInterestedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to remove interest: " + ex.getMessage(), null));
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse> getAllEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            return ResponseEntity.ok(new ApiResponse(true, "Events retrieved successfully", events));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to retrieve events: " + ex.getMessage(), null));
        }
    }
}