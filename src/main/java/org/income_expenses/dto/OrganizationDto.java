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
public class OrganizationDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String organizationName;

    @NotNull(message = "Категория организации / источника должна быть указана")
    private TransactionCategory category;
}
