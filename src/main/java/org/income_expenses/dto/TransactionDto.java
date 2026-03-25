package org.income_expenses.dto;

import lombok.Data;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private int amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime whenPerformed;
    private TransactionType transactionType;
    private TransactionCategory category;
}
