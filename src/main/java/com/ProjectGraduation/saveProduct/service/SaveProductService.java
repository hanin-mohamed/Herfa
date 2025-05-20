package com.ProjectGraduation.saveProduct.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
@Service
public class SaveProductService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JWTService jwtService ;


    @Transactional
    public void saveProduct(Long productId, String token) {

        String userUserName = jwtService.getUsername(token.replace("Bearer " , ""));
        User user = getUserByUsername(userUserName) ;


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));

        if (user.getSavedProducts().contains(product)) {
            throw new IllegalStateException("Product is Already  saved by the user.");
        }


        user.getSavedProducts().add(product);
        userRepository.save(user) ;

    }

    @Transactional
    public void unSaveProduct(Long productId, String token) {

        String userUserName = jwtService.getUsername(token.replace("Bearer " , ""));
        User user = getUserByUsername(userUserName) ;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));

        if (!user.getSavedProducts().contains(product)) {
            throw new IllegalStateException("Product is not saved by the user.");
        }

        user.getSavedProducts().remove(product);

        userRepository.save(user);
    }
    @Transactional
    public List<Product> getAllSavedProducts(String token){
        String userUserName = jwtService.getUsername(token.replace("Bearer " , ""));

        User user = getUserByUsername(userUserName);

        return user.getSavedProducts();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }
}


