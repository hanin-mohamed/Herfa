package com.ProjectGraduation.comment.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
import com.ProjectGraduation.comment.entity.Comment;
import com.ProjectGraduation.comment.repo.CommentRepo;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepo repo ;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepo userRepo ;

    public Comment addComment(Long userId , Long productId , String content){
        Optional<User>userOptional = userRepo.findById(userId) ;
        Optional<Product>productOptional = productRepository.findById(productId);
        if (userOptional.isEmpty()||productOptional.isEmpty()){
            throw new RuntimeException("User Or Product not found ") ;
        }
        Comment comment = new Comment(content,userOptional.get(),productOptional.get());
        return repo.save(comment) ;
    }

    public List<Comment> getCommentByProductId(Long productId){
        Optional<Product>productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()){
            throw new RuntimeException("Product not Found") ;
        }
        return repo.findByProduct(productOptional.get()) ;
    }

}
