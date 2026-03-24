package org.income_expenses.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.WalletMember;
import org.income_expenses.models.WalletMemberRole;
import org.income_expenses.repositories.FamilyWalletRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public FamilyWallet createWallet(String walletName, MyUser currentUser) {
        FamilyWallet wallet = new FamilyWallet();

        wallet.setOwner(currentUser);
        wallet.setName(walletName);
        wallet.setCreatedBy(currentUser);
        wallet.setCreatedAt(LocalDateTime.now());

        WalletMember member = new WalletMember();

        member.setWallet(wallet);
        member.setMember(currentUser);
        member.setRole(WalletMemberRole.OWNER);
        member.setCreatedBy(currentUser);
        member.setCreatedAt(LocalDateTime.now());

        wallet.getMembers().add(member);

        this.saveWallet(wallet);

        return wallet;
    }

    public FamilyWallet saveWallet(FamilyWallet wallet) {
        return walletRepository.save(wallet);
    }
}
