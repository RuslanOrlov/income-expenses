package org.income_expenses.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.models.*;
import org.income_expenses.repositories.FamilyWalletRepository;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.income_expenses.repositories.WalletMemberRepository;
import org.income_expenses.repositories.WalletTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeService {

    private final FamilyWalletRepository familyWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final WalletMemberRepository walletMemberRepository;

    public List<WalletTransaction> getIncomeTransactions(MyUser currentUser, FamilyWallet wallet,
                                                         int curPage, int pageSize) {
        Pageable pageable = PageRequest.of(curPage, pageSize, Sort.by("id"));
        Page<WalletTransaction> page =
                walletTransactionRepository.getIncomeTransactions(currentUser.getId(), wallet.getId(), pageable);
        return page.getContent();
    }

    public WalletTransaction getIncomeCard(Long id) {
        WalletTransaction transaction = walletTransactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No income transaction found"));
        return transaction;
    }

    @Transactional
    public void createIncomeTransaction(TransactionDto transaction, MyUser currentUser) {
        FamilyWallet wallet = getWalletMember(currentUser).getWallet();

        WalletTransaction newTransaction= WalletTransaction.builder()
                .wallet(wallet)
                .amount(BigDecimal.valueOf(transaction.getAmount()))
                .whoPerformed(currentUser)
                .whenPerformed(transaction.getWhenPerformed())
                .transactionType(transaction.getTransactionType())
                .category(transaction.getCategory())
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        List<WalletTransaction> transactions = wallet.getTransactions();
        transactions.add(newTransaction);
        wallet.setTransactions(transactions);
        wallet.setTotalAmount(wallet.getTotalAmount().add(newTransaction.getAmount()));

        familyWalletRepository.save(wallet);
    }

    @Transactional
    public void deleteIncome(Long id) {
        WalletTransaction transaction = walletTransactionRepository
                .findById(id).orElseThrow(() -> new NoSuchElementException("No income transaction found"));

        FamilyWallet wallet = transaction.getWallet();

        if (transaction.getCategory().equals(TransactionCategory.INCOME)) {
            wallet.setTotalAmount(wallet.getTotalAmount().subtract(transaction.getAmount()));
        } else {
            wallet.setTotalAmount(wallet.getTotalAmount().add(transaction.getAmount()));
        }

        List<WalletTransaction> transactions = wallet.getTransactions();
        transactions.remove(transaction);

        log.info("--- transaction to be deleted = {}", transaction);

        familyWalletRepository.save(wallet);

        // В сущности FamilyWallet в аннотации @OneToMany атрибут orphanRemoval = true,
        // благодаря чему не требуется вызывать операцию удаления (см. нижн. строку кода)
        walletTransactionRepository.deleteById(id);
    }

    public long incomeTransactionsCount(MyUser currentUser, FamilyWallet wallet) {
        return walletTransactionRepository.incomeTransactionsCount(currentUser.getId(), wallet.getId());
    }

    public List<TransactionType> getIncomeTransactionTypeList() {
        return transactionTypeRepository
                .findAll()
                .stream()
                .filter(type -> type.getCategory() == TransactionCategory.INCOME)
                .collect(Collectors.toList());
    }

    public WalletMember getWalletMember(MyUser user) {
        return walletMemberRepository
                .findByMember(user)
                .orElseThrow(() -> new NoSuchElementException("No wallet member found"));
    }
}
