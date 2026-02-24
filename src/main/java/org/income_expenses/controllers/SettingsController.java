package org.income_expenses.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String getSettings() {
        return "settings";
    }

    @GetMapping("/user-profile")
    public String getUserProfile(Model model,
                                 @AuthenticationPrincipal MyUser user) {
        model.addAttribute("user", user);
        return "user-profile";
    }

    @GetMapping("/cnahge-password")
    public String openChangePasswordForm(Model model,
                                         @AuthenticationPrincipal MyUser user,
                                         HttpServletRequest request) {
        model.addAttribute("user",
                ChangePasswordForm.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .email(user.getEmail())
                        .build());

        String referer = request.getHeader("Referer");

        return "user-password-change";
    }

    @PostMapping("/cnahge-password")
    public String changeUserPassword(
            Model model,
            @AuthenticationPrincipal MyUser currentUser,
            @Valid @ModelAttribute("user") ChangePasswordForm form,
            BindingResult errors) {

        if (errors.hasErrors() ||
                !form.isConfirmEqualsPassword() ||
                !passwordEncoder.matches(form.getCurrentPass(), currentUser.getPassword())) {
            // Доработать форму user-password-change, чтобы она выводила в форму
            // стандартные собщения из аннотаций валидации в классе ChangePasswordForm
            if (!form.isConfirmEqualsPassword())
                model.addAttribute("confirmPasswordError", true);

            if (!passwordEncoder.matches(form.getCurrentPass(), currentUser.getPassword()))
                model.addAttribute("currentPasswordError", true);

            return "user-password-change";
        };
        MyUser myUser = userService.getUserById(currentUser.getId());

        myUser.setPassword(passwordEncoder.encode(form.getPassword()));

        userService.saveUser(myUser);

        return "redirect:/settings";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.users());
        return "users-list";
    }
}
