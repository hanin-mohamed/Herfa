package com.ProjectGraduation.auth.repository;

//import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    //////  save product //////
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.savedProducts WHERE u.id = :userId")
    Optional<User> findByIdWithSavedProducts(@Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN u.favProducts p WHERE p.id = :productId")
    List<User> findUsersByFavProduct(@Param("productId") Long productId);

    User findUserById(Long id);
}