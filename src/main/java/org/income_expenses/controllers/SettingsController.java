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
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping(value = "/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserService userService;

    @GetMapping
    public String getSettings() {
        return "settings";
    }

    @GetMapping("/user-profile")
    public String getUserProfile(Model model,
                                 @AuthenticationPrincipal MyUser user) {
        model.addAttribute("user", user);
        model.addAttribute("returnTo", "/settings");
        return "user-profile";
    }

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("returnTo", "/settings/users");
        return "user-profile-for-admin-mode";
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

        return "user-password-change";
    }

    @PostMapping("/cnahge-password")
    public String changeUserPassword(
            Model model,
            @AuthenticationPrincipal MyUser currentUser,
            @Valid @ModelAttribute("user") ChangePasswordForm form,
            BindingResult errors) {

        if ( !userService.isCorrectNewPassword(
                currentUser.getId(),
                form,
                errors, model) ) {
            return "user-password-change";
        }
        return "redirect:/settings";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.users());
        return "users-list";
    }
}
