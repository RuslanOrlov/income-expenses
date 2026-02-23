package org.income_expenses.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.income_expenses.models.MyUser;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
public class RegisterForm {

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String username;
    @NotBlank(message = "Пароль пользователя не может быть пустым")
    private String password;
    private String confirm;
    private String role;
    private String email;

    public boolean isConfirmEqualsPassword() {
        return confirm.equals(password);
    }

    public MyUser toUser(PasswordEncoder passwordEncoder) {
        return MyUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role != null && !role.isEmpty() ? role : "USER")
                .email(email)
                .build();
    }
}
