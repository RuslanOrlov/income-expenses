package org.income_expenses.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItemDto {
    private Long id;

    @NotNull(message = "Сумма не может быть пустой")
    @Positive(message = "Сумма должна быть указана > 0")
    @Digits(integer = 10, fraction = 2, message = "Формат: до 10 цифр до запятой, 2 после")
    private BigDecimal amount;

    private String description;
}
