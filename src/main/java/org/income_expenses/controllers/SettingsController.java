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

    // Методы управления доступом к профилю своего
    // пользователя и изменением своего пароля

    @ModelAttribute("totalListElements")
    public long totalElements() {
        return userService.usersCount();
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

    // Методы управления постраничным просмотром списка пользователей
    @GetMapping("/users/prev")
    public String prevPageUsers(@AuthenticationPrincipal MyUser currentUser) {
        // Обновляем доступные текущему пользователю
        // количество элементов списка и количество страниц
        long totalElements = calculateTotalElements();
        int totalPages = calculateTotaPages(totalElements, currentUser.getPageSize());
        currentUser.setTotalElements(totalElements);
        currentUser.setTotalPages(totalPages);

        // Изменяем текущую страницу
        if (currentUser.getCurPage() > 0) {
            currentUser.setCurPage(currentUser.getCurPage() - 1);
        }

        // Сохраняем текущего пользователя
        userService.saveUser(currentUser);

        // Переходим в список пользователей
        return "redirect:/settings/users";
    }

    @GetMapping("/users/next")
    public String nextPageUsers(@AuthenticationPrincipal MyUser currentUser) {
        // Обновляем доступные текущему пользователю
        // количество элементов списка и количество страниц
        long totalElements = calculateTotalElements();
        int totalPages = calculateTotaPages(totalElements, currentUser.getPageSize());
        currentUser.setTotalElements(totalElements);
        currentUser.setTotalPages(totalPages);

        // Изменяем текущую страницу
        if (currentUser.getCurPage() < totalPages) {
            currentUser.setCurPage(currentUser.getCurPage() + 1);
        } else if (currentUser.getCurPage() > totalPages) {
            currentUser.setCurPage(totalPages);
        }

        // Сохраняем текущего пользователя
        userService.saveUser(currentUser);

        // Переходим в список пользователей
        return "redirect:/settings/users";
    }

    @PostMapping("/users/change-page-size")
    public String changePageSizeUsers(@ModelAttribute("current") MyUser current,
                                      @AuthenticationPrincipal MyUser currentUser) {
        // Это (ниже) временный "жеский" код
        if (current.getPageSize() < 1) {
            current.setPageSize(1);
        } else if (current.getPageSize() > 20) {
            current.setPageSize(20);
        }
        // Это (выше) временный "жеский" код
        currentUser.setPageSize(current.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/settings/users";
    }

    private long calculateTotalElements() {
        return userService.usersCount();
    }

    private int calculateTotaPages(long totalElements, int pageSize) {
        if (totalElements % pageSize == 0) {
            return (int) totalElements / pageSize - 1;
        }
        return (int) totalElements / pageSize;
    }

    // Методы управления списком пользователей
    @GetMapping("/users")
    public String getUsers(Model model, @AuthenticationPrincipal MyUser currentUser) {
        model.addAttribute("users", userService.users(
                currentUser.getCurPage(), currentUser.getPageSize()));
        model.addAttribute("current", currentUser);
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
