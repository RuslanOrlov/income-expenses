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
    public String register() {
        return "register";
    }

    @PostMapping
    public String registerUser(@Valid RegisterForm user,
                               BindingResult errors,
                               Model model) {
        if (errors.hasErrors() ||
                !user.isConfirmEqualsPassword() ||
                userService.checkUserExists(user.getUsername())) {

            if (errors.hasErrors()) {
                List<FieldError> fieldErrors = errors.getFieldErrors();
                for (FieldError fieldError : fieldErrors) {
                    if (fieldError.getField().equals("password"))
                        model.addAttribute("errorEmptyPass", true);
                    if (fieldError.getField().equals("username"))
                        model.addAttribute("errorEmptyName", true);
                }
            }

            if (!user.isConfirmEqualsPassword())
                model.addAttribute("errorMismatchPass", true);
            if (userService.checkUserExists(user.getUsername()))
                model.addAttribute("errorUser", true);
            return "register";
        }

        userService.saveUser(user.toUser(passwordEncoder));
        return "redirect:/login";
    }
}
