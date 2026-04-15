package org.income_expenses.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeSettingsForm {
    private Long id;
    private String username;

    @Positive(message = "Размер страницы должен быть положительным числом")
    @Max(value = 20, message = "Размер страницы не должен быть больше 20")
    private int pageSize;
}
