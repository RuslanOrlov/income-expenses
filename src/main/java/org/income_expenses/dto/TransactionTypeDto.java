package org.income_expenses.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.income_expenses.models.TransactionCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTypeDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String transactionTypeName;

    @NotNull(message = "Категория транзакции должна быть указана")
    private TransactionCategory category;
}
