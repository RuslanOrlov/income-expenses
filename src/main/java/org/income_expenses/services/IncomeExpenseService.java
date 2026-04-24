package org.income_expenses.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.dto.TransactionItemDto;
import org.income_expenses.models.*;
import org.income_expenses.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeExpenseService {

    private final FamilyWalletRepository familyWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final WalletMemberRepository walletMemberRepository;

    public FamilyWallet getFamilyWalletById(Long id) {
        return familyWalletRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public Page<WalletTransaction> getIncomeTransactions(MyUser currentUser, FamilyWallet wallet,
                                                         int curPage, int pageSize) {
        Pageable pageable = PageRequest.of(curPage, pageSize, Sort.by("id").descending());
        Page<WalletTransaction> page =
                walletTransactionRepository.getIncomeTransactions(currentUser.getId(), wallet.getId(), pageable);
        return page;
    }

    public Page<WalletTransaction> getExpenseTransactions(MyUser currentUser, FamilyWallet wallet,
                                                          int curPage, int pageSize) {
        Pageable pageable = PageRequest.of(curPage, pageSize, Sort.by("id").descending());
        Page<WalletTransaction> page =
                walletTransactionRepository.getExpenseTransactions(currentUser.getId(), wallet.getId(), pageable);
        return page;
    }

    public WalletTransaction getIncomeOrExpenseCard(Long id) {
        WalletTransaction transaction = walletTransactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        return transaction;
    }

    @Transactional
    public void createIncomeTransaction(TransactionDto transaction, MyUser currentUser) {
        FamilyWallet wallet = getWalletMember(currentUser).getWallet();

        WalletTransaction newTransaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(transaction.getAmount())
                .whoPerformed(currentUser)
                .whenPerformed(transaction.getWhenPerformed())
                .organization(transaction.getOrganization())
                .transactionType(transaction.getTransactionType())
                .category(transaction.getCategory())
                .description(transaction.getDescription())
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
    public void createExpenseTransaction(TransactionDto transaction, MyUser currentUser) {
        FamilyWallet wallet = getWalletMember(currentUser).getWallet();

        WalletTransaction newTransaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(transaction.getAmount().negate())
                .whoPerformed(currentUser)
                .whenPerformed(transaction.getWhenPerformed())
                .organization(transaction.getOrganization())
                .transactionType(transaction.getTransactionType())
                .category(transaction.getCategory())
                .description(transaction.getDescription())
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Process items of transaction
        List<TransactionItem> transactionItems = new ArrayList<>();
        for (TransactionItemDto itemDto : transaction.getItems()) {
            TransactionItem item = TransactionItem.builder()
                    .name(itemDto.getName())
                    .price(itemDto.getPrice())
                    .quantity(itemDto.getQuantity())
                    .amount(itemDto.getAmount())
                    .transaction(newTransaction)
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionItems.add(item);
        }
        newTransaction.setItems(transactionItems);

        List<WalletTransaction> transactions = wallet.getTransactions();
        transactions.add(newTransaction);
        wallet.setTransactions(transactions);
        wallet.setTotalAmount(wallet.getTotalAmount().add(newTransaction.getAmount()));

        familyWalletRepository.save(wallet);
    }

    public void changeIncomeOrExpenseTransaction(TransactionDto transaction, Long id) {
        // Получаем транзакцию
        WalletTransaction updated = this.getIncomeOrExpenseCard(id);
        // Обновляем транзакцию
        updated.setOrganization(transaction.getOrganization());
        updated.setTransactionType(transaction.getTransactionType());
        updated.setDescription(transaction.getDescription());
        // Сохраняем транзакцию
        walletTransactionRepository.save(updated);
    }

    @Transactional
    public void deleteIncomeOrExpense(Long id) {
        WalletTransaction transaction = walletTransactionRepository
                .findById(id).orElseThrow(() -> new NoSuchElementException("No income transaction found"));

        FamilyWallet wallet = transaction.getWallet();

        if (transaction.getCategory().equals(TransactionCategory.INCOME)) {
            wallet.setTotalAmount(wallet.getTotalAmount().subtract(transaction.getAmount()));
        } else {
            wallet.setTotalAmount(wallet.getTotalAmount().add(transaction.getAmount().abs()));
        }

        List<WalletTransaction> transactions = wallet.getTransactions();
        transactions.remove(transaction);

        log.info("--- transaction to be deleted = {}", transaction);

        familyWalletRepository.save(wallet);

        // ЕСЛИ бы в сущности FamilyWallet в аннотации @OneToMany для поля transactions был
        // явным образом инициализирован атрибут orphanRemoval = true, ТО не потребовалось
        // бы вызывать ниже следующую операцию удаления (см. след. строку кода):

        walletTransactionRepository.deleteById(id);
    }

    public long incomeTransactionsCount(MyUser currentUser, FamilyWallet wallet) {
        return walletTransactionRepository.incomeTransactionsCount(currentUser.getId(), wallet.getId());
    }

    public long expenseTransactionsCount(MyUser currentUser, FamilyWallet wallet) {
        return walletTransactionRepository.expenseTransactionsCount(currentUser.getId(), wallet.getId());
    }

    public List<TransactionType> getTransactionTypeList(TransactionCategory transactionCategory) {
        return transactionTypeRepository
                .findAll()
                .stream()
                .filter(type -> type.getCategory() == transactionCategory)
                .collect(Collectors.toList());
    }

    public List<Organization> getOrganizations(TransactionCategory transactionCategory) {
        return organizationRepository
                .findAll()
                .stream()
                .filter(org -> org.getCategory() == transactionCategory)
                .collect(Collectors.toList());
    }

    public WalletMember getWalletMember(MyUser user) {
        return walletMemberRepository
                .findByMember(user)
                .orElseThrow(() -> new NoSuchElementException("No wallet member found"));
    }
}
