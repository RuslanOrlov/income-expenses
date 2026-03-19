package org.income_expenses.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.FamilyWalletRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FamilyWalletRepository walletRepository;

    public boolean existsWalletByOwner(MyUser currentUser) {
        return walletRepository.existsByOwner(currentUser);
    }

    public FamilyWallet findWalletByOwner(MyUser currentUser) {
        FamilyWallet wallet = walletRepository.findByOwner(currentUser)
                .orElseThrow(()->new EntityNotFoundException("Wallet not found"));
        return wallet;
    }

    public FamilyWallet saveWallet(FamilyWallet wallet) {
        return walletRepository.save(wallet);
    }
}
