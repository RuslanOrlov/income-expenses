package org.income_expenses.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.income_expenses.dto.RegisterForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

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

        MyUser newUser = user.toUser();
        newUser.setPassword(userService.getEncodedPassword(newUser.getPassword()));
        userService.saveUser(newUser);

        return "redirect:/login";
    }


    @GetMapping("/admin-mode")
    public String openRegisterFormForAdminMode () {
        return "register-admin-mode";
    }

    @PostMapping("/admin-mode")
    public String registerUserForAdminMode(@Valid RegisterForm user,
                               BindingResult errors,
                               Model model) {
        if (!userService.isCorrectNewUser(user, errors, model)) {
            return "register-admin-mode";
        }

        MyUser newUser = user.toUser();
        newUser.setPassword(userService.getEncodedPassword(newUser.getPassword()));
        userService.saveUser(newUser);

        return "redirect:/settings/users";
    }
}
