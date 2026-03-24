package org.income_expenses.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.WalletTransaction;
import org.income_expenses.repositories.WalletTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final WalletTransactionRepository walletTransactionRepository;

    public List<WalletTransaction> getIncomeTransactions(MyUser currentUser, FamilyWallet wallet) {
        return walletTransactionRepository.getIncomeTransactions(currentUser.getId(), wallet.getId());
    }

    public WalletTransaction getIncomeCard(Long id) {
        return null;
    }

    public void createIncomeTransaction(@Valid WalletTransaction walletTransaction) {

    }

    public void deleteIncome(Long id) {

    }

    public long transactionsCount() {
        return walletTransactionRepository.count();
    }
}
