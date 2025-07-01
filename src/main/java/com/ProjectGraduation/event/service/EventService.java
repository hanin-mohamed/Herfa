package com.ProjectGraduation.event.service;

import com.ProjectGraduation.comment.dto.CommentResponse;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.exception.CommentNotFoundException;
import com.ProjectGraduation.comment.repo.CommentRepo;
import com.ProjectGraduation.event.dto.EventDto;
import com.ProjectGraduation.event.entity.Event;
import com.ProjectGraduation.event.exception.*;
import com.ProjectGraduation.event.helper.EventHelper;
import com.ProjectGraduation.event.repo.EventRepo;
import com.ProjectGraduation.auth.entity.User;

import com.ProjectGraduation.notification.entity.NotificationMessage;
import com.ProjectGraduation.notification.service.NotificationService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.exception.ProductNotFoundException;

import com.ProjectGraduation.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepo eventRepository;
    private final ProductRepository productRepository;
    private final CommentRepo commentRepo;
    private final EventHelper eventHelper;
    private final NotificationService notificationService;


    @Transactional
    public Event createEvent(String token, EventDto eventDto, MultipartFile file) throws IOException {
        try {
             User user = eventHelper.getUserFromToken(token);

            String uploadedFileName = eventHelper.uploadEventMedia(file, user.getId());

            Event event = eventHelper.buildEvent(eventDto, uploadedFileName, user);

            Event savedEvent = eventRepository.save(event);

            return savedEvent;
        } catch (Exception e) {
            System.err.println("Error in createEvent: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void expressInterest(Long eventId, String token) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        User user = eventHelper.getUserFromToken(token);

        if (event.getInterestedUsers().contains(user)) {
            throw new AlreadyInterestedException("User already interested");
        }

        event.addInterestedUser(user);
        eventRepository.save(event);
    }

    @Transactional
    public void removeInterest(Long eventId, String token) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        User user = eventHelper.getUserFromToken(token);

        if (!event.getInterestedUsers().contains(user)) {
            throw new NotInterestedException("User not interested");
        }

        event.removeInterestedUser(user);
        eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional
    public Event updateEvent(Long eventId, String token, EventDto eventDto, MultipartFile file) throws IOException {
        User user = eventHelper.validateMerchant(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        eventHelper.validateEventOwnership(event, user);

        if (file != null && !file.isEmpty()) {
            String newMedia = eventHelper.uploadEventMedia(file, user.getId());
            event.setMedia(newMedia);
        }

        event.setName(eventDto.getName());
        event.setDescription(eventDto.getDescription());
        event.setStartTime(eventDto.getStartTime());
        event.setEndTime(eventDto.getEndTime());
        event.setPrice(eventDto.getPrice());

        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId, String token) {
        User user = eventHelper.validateMerchant(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        eventHelper.validateEventOwnership(event, user);
        eventRepository.delete(event);
    }

    @Transactional
    public Event addProductToEvent(Long eventId, Long productId, String token) {
        User merchant = eventHelper.validateMerchant(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        eventHelper.validateEventOwnership(event, merchant);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        eventHelper.validateProductOwnership(product, merchant);

        event.addProduct(product);
        Event savedEvent = eventRepository.save(event);
        sendNewProductNotification(savedEvent, product);

        return savedEvent;    }


    private void sendNewProductNotification (Event event, Product product) {
        NotificationMessage notification = new NotificationMessage( );
        notification.setTitle("New Product Added to Event " + event.getName());
        notification.setBody("Product " + product.getName() + " has been added to the Event " + event.getName());
        notification.setImage(product.getMedia());
        notificationService.sendToInterestedUsers(event.getId(), notification);
    }


    @Transactional
    public Event removeProductFromEvent(Long eventId, Long productId, String token) {
        User merchant = eventHelper.validateMerchant(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        eventHelper.validateEventOwnership(event, merchant);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        event.removeProduct(product);
        return eventRepository.save(event);
    }

    public List<Product> getEventProducts(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        return new ArrayList<>(event.getProducts());
    }

    @Transactional
    public Event addCommentToEvent(Long eventId, String token, String commentText) {
        User user = eventHelper.getUserFromToken(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setUser(user);
        comment.setContent(commentText);

        event.addComment(comment);
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateComment(Long eventId,long commentId , String token, String commentText) {

        User user = eventHelper.getUserFromToken(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        Comment comment =  commentRepo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only update your own comments");
        }
        comment.setContent(commentText);
        comment.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);

    }

    @Transactional
    public void deleteComment(Long eventId, Long commentId, String token) throws UnauthorizedAccessException {
        User user = eventHelper.getUserFromToken(token);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only delete your own comments");
        }
        event.removeComment(comment);
        eventRepository.save(event);
    }

    public List<CommentResponse> getComments(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        return new ArrayList<>(event.getCommentResponses());
    }

    @Transactional
    public List<Event> getUserInterestedEvents(String token) {
        User user = eventHelper.getUserFromToken(token);

        List<Event> events = eventRepository.findEventsByInterestedUserId(user.getId());
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for the user with ID: " + user.getId());
        }
        return events;
    }
    public List<Event> getAllInterestedEvents(String token) {
        User user = eventHelper.getUserFromToken(token);
        List<Event> events = eventRepository.findEventsByInterestedUserId(user.getId());
        if (events.isEmpty()) {
            throw new EventNotFoundException("No events found for the user with ID: " + user.getId());
        }
        return events;
    }


}