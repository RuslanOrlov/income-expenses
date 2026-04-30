package org.income_expenses.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
}
