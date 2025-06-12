package com.ProjectGraduation.event.controller;

import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.exception.CommentNotFoundException;
import com.ProjectGraduation.event.dto.EventDto;
import com.ProjectGraduation.event.entity.Event;
import com.ProjectGraduation.event.exception.AlreadyInterestedException;
import com.ProjectGraduation.event.exception.EventNotFoundException;
import com.ProjectGraduation.event.exception.NotInterestedException;
import com.ProjectGraduation.event.exception.UnauthorizedAccessException;
import com.ProjectGraduation.event.service.EventService;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.FileUploadException;
import com.ProjectGraduation.product.exception.UnauthorizedMerchantException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import java.io.IOException;
import java.util.List;
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse> createEvent(
            @RequestHeader("Authorization") String token,
            @ModelAttribute EventDto eventDto,
            @RequestPart MultipartFile file
    ) throws IOException {
        try {
            Event createdEvent = eventService.createEvent(token, eventDto, file);
            return ResponseEntity.ok(new ApiResponse(true, "Event created successfully", createdEvent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateEvent(
            @RequestParam Long eventId,
            @RequestHeader("Authorization") String token,
            @ModelAttribute EventDto eventDto,
            @RequestPart(required = false) MultipartFile file
    ) {
        try {
            Event updatedEvent = eventService.updateEvent(eventId, token, eventDto, file);
            return ResponseEntity.ok(new ApiResponse(true, "Event updated successfully", updatedEvent));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse> deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            eventService.deleteEvent(eventId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Event deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }

    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(new ApiResponse(true, "Events fetched successfully", events));
    }

    @PostMapping("/{eventId}/interest")
    public ResponseEntity<ApiResponse> expressInterest(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            eventService.expressInterest(eventId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Interest expressed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{eventId}/interest")
    public ResponseEntity<ApiResponse> removeInterest(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            eventService.removeInterest(eventId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Interest removed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/{eventId}/products/{productId}")
    public ResponseEntity<ApiResponse> addProductToEvent(
            @PathVariable Long eventId,
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            Event event = eventService.addProductToEvent(eventId, productId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Product added to event", event));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{eventId}/products/{productId}")
    public ResponseEntity<ApiResponse> removeProductFromEvent(
            @PathVariable Long eventId,
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            Event event = eventService.removeProductFromEvent(eventId, productId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Product removed from event", event));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{eventId}/products")
    public ResponseEntity<ApiResponse> getEventProducts(
            @PathVariable Long eventId
    ) {
        try {
            List<Product> products = eventService.getEventProducts(eventId);
            return ResponseEntity.ok(new ApiResponse(true, "Products fetched successfully", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/{eventId}/comments")
    public ResponseEntity<ApiResponse> addCommentToEvent(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String token,
            @RequestParam String commentText
    ) {
        try {
            Event event = eventService.addCommentToEvent(eventId, token, commentText);
            return ResponseEntity.ok(new ApiResponse(true, "Comment added to event", event));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{eventId}/comments/{commentId}")
    public ResponseEntity<ApiResponse> updateComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token,
            @RequestParam String commentText) {

        try {
            Event updatedEvent = eventService.updateComment(
                    eventId,
                    commentId,
                    token.replace("Bearer ", ""),
                   commentText
            );
            return ResponseEntity.ok(
                    new ApiResponse(true, "Comment updated successfully", updatedEvent)
            );
        } catch (EventNotFoundException | CommentNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (UnauthorizedAccessException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to update comment", null));
        }
    }

    @DeleteMapping("/{eventId}/comments/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            eventService.deleteComment(eventId, commentId, token);
            return ResponseEntity.ok(new ApiResponse(true, "Comment deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{eventId}/comments")
    public ResponseEntity<ApiResponse> getEventComments(
            @PathVariable Long eventId
    ) {
        try {
            List<Comment> comments = eventService.getComments(eventId);
            return ResponseEntity.ok(new ApiResponse(true, "Comments fetched successfully", comments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

}
