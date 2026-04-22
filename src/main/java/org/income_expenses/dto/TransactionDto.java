package org.income_expenses.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long id;

    @NotNull(message = "Сумма не может быть пустой")
    @Positive(message = "Сумма должна быть указана > 0")
    @Digits(integer = 10, fraction = 2, message = "Формат: до 10 цифр до запятой, 2 после")
    private BigDecimal amount;

    @NotNull(message = "Дата не может быть пустой")
    @PastOrPresent(message = "Дата не может быть в будущем")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime whenPerformed;

    @NotNull(message = "Значение поля должно быть указано")
    private Organization organization;

    @NotNull(message = "Тип транзакции должен быть указан")
    private TransactionType transactionType;

    @NotNull(message = "Категория транзакции должна быть указана")
    private TransactionCategory category;

    private List<TransactionItemDto> items = new ArrayList<>();

    String description;
}
