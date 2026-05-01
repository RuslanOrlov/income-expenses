package org.income_expenses.repositories;

import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long> {

    boolean existsByTransactionTypeNameIgnoringCaseAndCategory(String transactionTypeName, TransactionCategory category);

    boolean existsByTransactionTypeNameIgnoringCase(String transactionTypeName);

    boolean existsByTransactionTypeNameIgnoringCaseAndCategoryAndIdNot(String transactionTypeName, TransactionCategory category, Long id);
}
