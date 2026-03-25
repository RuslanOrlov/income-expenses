package org.income_expenses.dto;

import jakarta.persistence.*;
import lombok.Data;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionItem;
import org.income_expenses.models.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionDto {
    private int amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime whenPerformed;
    private TransactionType transactionType;
    private TransactionCategory category;
}
