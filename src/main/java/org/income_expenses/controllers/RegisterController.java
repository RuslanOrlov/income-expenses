package org.income_expenses.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.income_expenses.dto.RegisterForm;
import org.income_expenses.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String openRegisterForm() {
        return "register";
    }

    @PostMapping
    public String registerUser(@Valid RegisterForm user,
                               BindingResult errors,
                               Model model) {
        if (!userService.isCorrectNewUser(user, errors, model)) {
            return "register";
        }
        return "redirect:/login";
    }


    @GetMapping("/admin-mode")
    public String openRegisterFormForAdminMode () {
        return "register-for-admin-mode";
    }

    @PostMapping("/admin-mode")
    public String registerUserForAdminMode(@Valid RegisterForm user,
                               BindingResult errors,
                               Model model) {
        if (!userService.isCorrectNewUser(user, errors, model)) {
            return "register-for-admin-mode";
        }
        return "redirect:/settings/users";
    }
}
