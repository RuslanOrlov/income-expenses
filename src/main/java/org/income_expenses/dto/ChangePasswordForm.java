package org.income_expenses.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordForm {

    private Long id;
    private String username;
    @NotBlank(message = "Текущий пароль пользователя не может быть пустым")
    private String currentPass;
    @NotBlank(message = "Новый пароль не может быть пустым")
    private String password;
    @NotBlank(message = "Подтверждение нового пароля не может быть пустым")
    private String confirm;
    private String role;
    private String email;

    public boolean isConfirmEqualsPassword() {
        return confirm.equals(password);
    }
}
