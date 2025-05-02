package com.ProjectGraduation.comment.controller;

import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JWTService jwtService;
    private final UserRepo repo;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> addComment(@RequestHeader("Authorization") String token,
                                                  @RequestParam Long productId,
                                                  @RequestParam String content) {
        String userName = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = repo.findByUsernameIgnoreCase(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentService.addComment(user.getId(), productId, content);
        return ResponseEntity.ok(new ApiResponse(true, "Comment added successfully!", comment));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse> getCommentsByProduct(@PathVariable Long productId) {
        List<Comment> comments = commentService.getCommentByProductId(productId);
        return ResponseEntity.ok(new ApiResponse(true, "Comments fetched successfully!", comments));
    }
}
