package com.ProjectGraduation.FavProduct.service;

import com.ProjectGraduation.auth.entity.User;
import com.ProjectGraduation.auth.entity.repo.UserRepo;
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

    private final UserRepo userRepo ;

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
        userRepo.save(user) ;


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
        userRepo.save(user);

    }

    public User getUserByUserName(String username){
        return userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(()->new RuntimeException("User Not Found")) ;
    }

    public List<User> getUsersByFavProduct(Long productId) {
        return userRepo.findUsersByFavProduct(productId);
    }
}
