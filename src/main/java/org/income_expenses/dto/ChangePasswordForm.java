package org.income_expenses.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordForm {

    private Long id;
    private String username;
    @NotNull(message = "Текущий пароль пользователя не может быть null")
    private String currentPass;
    @NotBlank(message = "Новый пароль не может быть пустым")
    private String newPassword;
    @NotBlank(message = "Подтверждение нового пароля не может быть пустым")
    private String confirm;
    private String role;
    private String email;
    private String mode;
    private String accountType;

    public boolean isConfirmEqualsNewPassword() {
        return confirm.equals(newPassword);
    }
}
