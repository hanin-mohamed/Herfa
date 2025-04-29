package com.ProjectGraduation.FavProduct.controller;

import com.ProjectGraduation.FavProduct.service.FavService;
import com.ProjectGraduation.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favourites")
@RequiredArgsConstructor
public class FavController {

    private final FavService service ;


    @PostMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> saveFavProduct(@PathVariable Long productId ,
                                                 @RequestHeader("Authorization") String token
                                                 ){


        try {
            service.favProduct(productId,token);
            return ResponseEntity.ok("Done !!!") ;
        }
        catch (IllegalStateException ex){
            return ResponseEntity.status(401).body("Unauthorized " + ex.getMessage());
        }catch (Exception ex){
            return ResponseEntity.badRequest().body("Error : " + ex.getMessage());
        }
    }


    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> unFavProduct(@PathVariable Long productId,
                                                @RequestHeader("Authorization") String token
    ) {
        try {
            service.UnFavProduct(productId, token);
            return ResponseEntity.ok("Product unFav successfully!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<List<User>> getUsersByFavProduct(@PathVariable Long productId) {
        List<User> users = service.getUsersByFavProduct(productId);
        return ResponseEntity.ok(users);
    }
}
