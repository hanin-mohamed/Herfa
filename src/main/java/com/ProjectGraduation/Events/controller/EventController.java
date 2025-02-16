package com.ProjectGraduation.Events.controller;

import com.ProjectGraduation.Events.entity.Event;
import com.ProjectGraduation.Events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<Event> createEvent(@RequestHeader("Authorization") String token,
                                             @RequestPart("name") String name,
                                             @RequestPart("description") String description,
                                             @RequestPart("media") MultipartFile media,
                                             @RequestPart("endTime") String endTime,
                                             @RequestPart(value = "price", required = false) Double price) {
        LocalDateTime end = LocalDateTime.parse(endTime.trim());
        Event event = eventService.createEvent(token, name, description, media, end, price);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/interest")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> expressInterest(@PathVariable Long eventId,
                                                  @RequestHeader("Authorization") String token) {
        eventService.expressInterest(eventId, token);
        return ResponseEntity.ok("Interest added!");
    }

    @DeleteMapping("/{eventId}/interest")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> removeInterest(@PathVariable Long eventId,
                                                 @RequestHeader("Authorization") String token) {
        eventService.removeInterest(eventId, token);
        return ResponseEntity.ok("Interest removed!");
    }

    @GetMapping("")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

}
