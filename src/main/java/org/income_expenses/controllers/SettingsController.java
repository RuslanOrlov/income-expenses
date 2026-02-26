package org.income_expenses.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
        model.addAttribute("mode", "user");
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
        model.addAttribute("returnToWhenGet", "/settings/user-profile");
        model.addAttribute("returnToWhenPost", "/settings/cnahge-password");

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

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("returnTo", "/settings/users");
        model.addAttribute("mode", "admin");
        return "user-profile";
    }

    @GetMapping("/users/{id}/cnahge-password")
    public String openChangePasswordFormAdminMode(
            @PathVariable("id") Long id,
            Model model,
            HttpServletRequest request) {

        MyUser user = userService.getUserById(id);

        model.addAttribute("user",
                ChangePasswordForm.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .email(user.getEmail())
                        .build());
        model.addAttribute("returnToWhenGet", "/settings/users");
        model.addAttribute("returnToWhenPost", "/settings/users/cnahge-password");

        return "user-password-change";
    }

    @PostMapping("/users/cnahge-password")
    public String changeUserPasswordAdminMode(
            Model model,
            @Valid @ModelAttribute("user") ChangePasswordForm form,
            BindingResult errors) {

        if ( !userService.isCorrectNewPassword(
                form.getId(),
                form,
                errors, model) ) {
            return "user-password-change";
        }
        return "redirect:/settings/users";
    }

    @GetMapping("/users/{id}/confirm-user-locking")
    public String userLockingConfirm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));

        model.addAttribute("returnTo", "/settings/users");

        return "confirm-user-locking";
    }


    @PostMapping("/users/user-locking")
    public String userLocking(@RequestParam("id") Long id,
                              @AuthenticationPrincipal MyUser currentUser,
                              Authentication authentication,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        if (userService.lockUser(id, currentUser) && authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/";
        }

        return "redirect:/settings/users";
    }

}
