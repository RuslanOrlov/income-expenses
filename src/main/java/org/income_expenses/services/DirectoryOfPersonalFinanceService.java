package org.income_expenses.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.income_expenses.dto.TransactionTypeDto;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DirectoryOfPersonalFinanceService {

    private final TransactionTypeRepository transactionTypeRepository;

    public Page<TransactionType> getAllTransactionTypes(int curPage, int pageSize) {
        Pageable pageable = PageRequest.of(curPage, pageSize, Sort.by("id"));
        return transactionTypeRepository.findAll(pageable);
    }

    public TransactionType getTransactionTypeById(Long id) {
        return transactionTypeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaction type not found"));
    }

    public void createTransactionType(TransactionTypeDto transactionType) {
        TransactionType newTransactionType = TransactionType
                .builder()
                .transactionTypeName(transactionType.getTransactionTypeName())
                .category(transactionType.getCategory())
                .createdAt(LocalDateTime.now())
                .build();
        transactionTypeRepository.save(newTransactionType);
    }

    public void editTransactionType(Long id, TransactionTypeDto transactionType) {
        TransactionType updated = getTransactionTypeById(id);
        updated.setTransactionTypeName(transactionType.getTransactionTypeName());
        updated.setCategory(transactionType.getCategory());
        transactionTypeRepository.save(updated);
    }

    public void deleteTransactionType(Long id) {
        transactionTypeRepository.deleteById(id);
    }
}
