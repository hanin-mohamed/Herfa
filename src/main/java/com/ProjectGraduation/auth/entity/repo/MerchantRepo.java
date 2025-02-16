package com.ProjectGraduation.auth.entity.repo;

import com.ProjectGraduation.auth.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface MerchantRepo extends JpaRepository<Merchant,Long> {

    Optional<Merchant> findByUsernameIgnoreCase(String username);

    Optional<Merchant> findByEmailIgnoreCase(String email);
}
