package com.ProjectGraduation.comment.controller;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService ;
    @Autowired
    private JWTService jwtService ;
    @Autowired
    private UserRepo repo ;

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String token,
                                        @RequestParam Long productId,
                                        @RequestParam String content) {
        String userName = jwtService.getUsername(token.replace("Bearer ",""));

        Optional<User> user = repo.findByUsernameIgnoreCase(userName);

        Long userId = user.get().getId();
        Comment comment = commentService.addComment(userId, productId, content);
        return ResponseEntity.ok(comment);
    }


    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<Comment>> getCommentsByProduct(@PathVariable Long productId) {
        List<Comment> comments = commentService.getCommentByProductId(productId);
        return ResponseEntity.ok(comments);
    }
}
