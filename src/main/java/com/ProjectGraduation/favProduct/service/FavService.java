package com.ProjectGraduation.favProduct.service;

import com.ProjectGraduation.auth.entity.Role;
import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.exception.UserNotFoundException;
import com.ProjectGraduation.auth.repository.UserRepository;
import com.ProjectGraduation.auth.service.JWTService;
import com.ProjectGraduation.product.entity.Product;
import com.ProjectGraduation.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FavService {

    private final JWTService jwtService ;

    private final UserRepository userRepository;

    private final ProductRepository repo ;


    @Transactional
    public void favProduct(Long productId , String token){
        String userUserName =
                jwtService.getUsername(token.replace("Bearer " , ""));
        User user = getUserByUserName(userUserName) ;

        Product product = repo.findById(productId)
                .orElseThrow(()->new  NoSuchElementException("Product Not Found With Id "+productId));

        if (user.getFavProducts().contains(product)){
            throw new IllegalArgumentException("Product Is Already Fav By This User");
        }
        user.getFavProducts().add(product);
        userRepository.save(user) ;


    }

    @Transactional
    public void UnFavProduct(Long productId , String token) {
        String userUserName =
                jwtService.getUsername(token.replace("Bearer ",""));
        User user = getUserByUserName(userUserName) ;

        Product product = repo.findById(productId)
                .orElseThrow(()->new NoSuchElementException("Product Not Found"));
        if (!user.getFavProducts().contains(product)){
            throw new IllegalArgumentException("Product is not fav by the user.");
        }
        user.getFavProducts().remove(product) ;
        userRepository.save(user);

    }
    public List<Product>getAllFavProducts(String token) {
        String userUserName =
                jwtService.getUsername(token.replace("Bearer ",""));
        User user = getUserByUserName(userUserName) ;
        return user.getFavProducts();
    }


    public User getUserByUserName(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
    public List<User> getUsersByFavProduct(Long productId) {
        return userRepository.findUsersByFavProduct(productId);
    }
}
