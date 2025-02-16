package com.ProjectGraduation.auth.entity.repo;

import com.ProjectGraduation.auth.entity.Merchant;
import com.ProjectGraduation.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    //////  save product //////
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.savedProducts WHERE u.id = :userId")
    Optional<User> findByIdWithSavedProducts(@Param("userId") Long userId);

    //////interest to merchant /////
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.interestedMerchants WHERE u.id = :userId")
    Optional<User> findByIdWithInterestedMerchants(@Param("userId") Long userId);

    /////// merchant get all user there are interested /////
    @Query("SELECT u FROM User u JOIN u.interestedMerchants m WHERE m.id = :merchantId")
    List<User> findUsersByInterestedMerchant(@Param("merchantId") Long merchantId);

    ///// user get all merchant that is interested /////
    @Query("SELECT m FROM Merchant m JOIN m.interestedUsers u WHERE u.id = :userId")
    List<Merchant> findMerchantUserInterest(@Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN u.favProducts p WHERE p.id = :productId")
    List<User> findUsersByFavProduct(@Param("productId") Long productId);

}
