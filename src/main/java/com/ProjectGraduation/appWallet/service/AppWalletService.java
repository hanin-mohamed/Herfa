package com.ProjectGraduation.appWallet.service;

import com.ProjectGraduation.appWallet.repository.AppWalletRepository;
import com.ProjectGraduation.appWallet.entity.AppWallet;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppWalletService {

    private final AppWalletRepository appWalletRepository;

    @PostConstruct
    public void init() {
        if (appWalletRepository.findById(1L).isEmpty()) {
            AppWallet wallet = new AppWallet();
            wallet.setId(1L);
            wallet.setAppBalance(0.0);
            wallet.setHeldForSellers(0.0);
            appWalletRepository.save(wallet);
        }
    }

    public AppWallet getWallet() {
        return appWalletRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("App wallet not found"));
    }

    @Transactional
    public void holdAmountForSeller(double amount) {
        AppWallet wallet = getWallet();
        wallet.setHeldForSellers(wallet.getHeldForSellers() + amount);
        appWalletRepository.save(wallet);
    }

    @Transactional
    public void releaseToSeller(double amount) {
        AppWallet wallet = getWallet();
        if (wallet.getHeldForSellers() < amount) {
            throw new IllegalArgumentException("Insufficient held balance");
        }
        wallet.setHeldForSellers(wallet.getHeldForSellers() - amount);
        appWalletRepository.save(wallet);
    }

    @Transactional
    public void increaseAppBalance(double commission) {
        AppWallet wallet = getWallet();
        wallet.setAppBalance(wallet.getAppBalance() + commission);
        appWalletRepository.save(wallet);
    }
    @Transactional
    public void deductFromAppForRefund(double amount) {
        AppWallet wallet = getWallet();
        if (wallet.getAppBalance() < amount) {
            throw new IllegalArgumentException("App balance is insufficient for refund.");
        }
        wallet.setAppBalance(wallet.getAppBalance() - amount);
        appWalletRepository.save(wallet);
    }

    @Transactional
    public void decreaseHeldForSellers(double amount) {
        AppWallet wallet = getWallet();
        double current = wallet.getHeldForSellers();
        if (current < amount) {
            throw new IllegalArgumentException("Not enough held funds to release");
        }
        wallet.setHeldForSellers(current - amount);
        appWalletRepository.save(wallet);
    }

    @Transactional
    public void resetWallet() {
        AppWallet wallet = getWallet();
        wallet.setAppBalance(0.0);
        wallet.setHeldForSellers(0.0);
        appWalletRepository.save(wallet);
    }
}
