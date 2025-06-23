package com.ProjectGraduation.appWallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "app_wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppWallet {

    @Id
    private Long id = 1L;

    private double appBalance;

    private double heldForSellers;
}
