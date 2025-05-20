package com.ProjectGraduation.comment.controller;

import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.exception.CommentNotFoundException;
import com.ProjectGraduation.comment.exception.UnauthorizedCommentDeletionException;
import com.ProjectGraduation.common.ApiResponse;
import com.ProjectGraduation.comment.service.CommentService;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> addComment(
            @RequestHeader("Authorization") String token,
            @RequestParam Long productId,
            @RequestParam String content) {
        try {
            Comment comment = commentService.addComment(token, productId, content);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Comment added successfully", comment));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getCommentsByProduct(
            @PathVariable Long productId) {
        try {
            var comments = commentService.getCommentByProductId(productId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Comments retrieved successfully", comments));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {
        try {

            commentService.deleteComment(commentId, token);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Comment deleted successfully", null));
        } catch (CommentNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(false, ex.getMessage(), null)
            );
        } catch (UnauthorizedCommentDeletionException ex) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    @DeleteMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse> deleteAllProductComments(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String token) {
        try {
            commentService.deleteAllCommentsForProduct(productId, token);
            return ResponseEntity.ok(
                    new ApiResponse(true, "All comments deleted successfully", null));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }
}