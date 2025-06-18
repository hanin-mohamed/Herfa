package com.ProjectGraduation.comment.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.comment.dto.CommentResponse;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.exception.CommentNotFoundException;
import com.ProjectGraduation.comment.exception.UnauthorizedCommentDeletionException;
import com.ProjectGraduation.comment.repo.CommentRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentService {


    private final CommentRepo repo ;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Transactional
    public Comment addComment(String token, Long productId, String content) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Comment comment = new Comment(content, user, product);
        return repo.save(comment);
    }

    public List<CommentResponse> getCommentByProductId(Long productId){
        Optional<Product>productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()){
            throw new RuntimeException("Product not Found") ;
        }
        return repo.findByProduct(productOptional.get()).stream()
                .map(this::convertToCommentResponse).toList();
    }

    public CommentResponse convertToCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getUser().getFirstName(),
                comment.getUser().getLastName(),
                comment.getUser().getId(),
                comment.getProduct().getId()
        );
    }

    @Transactional
    public void deleteComment(Long commentId, String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User requestingUser = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = repo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        validateCommentDeletionPermission(comment, requestingUser);
        repo.delete(comment);
    }

    private void validateCommentDeletionPermission(Comment comment, User requestingUser) {
        boolean isCommentOwner = comment.getUser().getId().equals(requestingUser.getId());
        boolean isProductOwner = comment.getProduct().getUser().getId().equals(requestingUser.getId());

        if (!isCommentOwner && !isProductOwner) {
            throw new UnauthorizedCommentDeletionException(
                    "You are not authorized to delete this comment");
        }
    }

    @Transactional
    public void deleteAllCommentsForProduct(Long productId, String token) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User merchant = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUser().getId().equals(merchant.getId())) {
            throw new UnauthorizedCommentDeletionException(
                    "Only the product owner can delete all comments");
        }

        repo.deleteByProductId(productId);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String token, String newContent) {
        String username = jwtService.getUsername(token.replace("Bearer ", ""));
        User requestingUser = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = repo.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(requestingUser.getId())) {
            throw new UnauthorizedCommentDeletionException(
                    "You are not authorized to update this comment");
        }

            comment.setContent(newContent);
            Comment updatedComment = repo.save(comment);
            return convertToCommentResponse(updatedComment) ;

    }
}
