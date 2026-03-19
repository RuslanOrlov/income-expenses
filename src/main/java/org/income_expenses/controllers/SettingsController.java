package org.income_expenses.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.services.SessionService;
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
    private final SessionService sessionService;

    @GetMapping
    public String settings() {
        return "home-page-of-settings";
    }

    // Методы управления доступом к профилю своего
    // пользователя и изменением своего пароля

    @ModelAttribute("currentUser")
    public MyUser currentUser(@AuthenticationPrincipal MyUser user) {
        return user;
    }

    @ModelAttribute("totalElementsList")
    public long totalElements() {
        return userService.usersCount();
    }

    @GetMapping("/user-profile")
    public String getUserProfile(Model model) {
        model.addAttribute("returnTo", "/settings");
        model.addAttribute("mode", "user");
        return "user-profile";
    }

    @GetMapping("/change-password")
    public String openChangePasswordForm(Model model,
                                         @AuthenticationPrincipal MyUser user) {

        ChangePasswordForm form = ChangePasswordForm.builder()
                .id(user.getId())
                .username(user.getUsername())
                .currentPass("*".equals(user.getPassword()) ? user.getPassword() : null)
                .role(user.getRole())
                .email(user.getEmail())
                .mode("user")
                .accountType(user.getAccountType())
                .build();

        model.addAttribute("user",
                form);

        log.info("--- ChangePasswordForm (GET) = {}", form);

        model.addAttribute("returnToWhenGet", "/settings/user-profile");
        model.addAttribute("returnToWhenPost", "/settings/change-password");

        return "user-password-change";
    }

    @PostMapping("/change-password")
    public String changeUserPassword(
            Model model,
            @AuthenticationPrincipal MyUser currentUser,
            @Valid @ModelAttribute("user") ChangePasswordForm form,
            BindingResult errors) {

        log.info("--- ChangePasswordForm (POST) = {}", form);

        if ( !userService.isCorrectNewPassword(
                currentUser.getId(),
                form,
                errors, model, form.getMode(), form.getAccountType()) ) {
            model.addAttribute("returnToWhenGet", "/settings/user-profile");
            model.addAttribute("returnToWhenPost", "/settings/change-password");
            return "user-password-change";
        }
        currentUser.setPassword(userService.getEncodedPassword(form.getNewPassword()));
        if ("GOOGLE".equals(currentUser.getAccountType())) {
            currentUser.setAccountType("LOCAL");
        }

        userService.saveUser(currentUser);

        return "redirect:/settings";
    }

    // Методы управления постраничным просмотром списка пользователей
    @GetMapping("/users/prev")
    public String prevPageUsers(@AuthenticationPrincipal MyUser currentUser) {
        // Изменяем текущую страницу И сохраняем текущего пользователя
        if (currentUser.getCurPage() > 1) {
            currentUser.setCurPage(currentUser.getCurPage() - 1);
            userService.saveUser(currentUser);
        }

        // Переходим в список пользователей
        return "redirect:/settings/users";
    }

    @GetMapping("/users/next")
    public String nextPageUsers(@AuthenticationPrincipal MyUser currentUser) {
        // Изменяем текущую страницу пользователя
        if (currentUser.getCurPage() < currentUser.getTotalPages()) {
            currentUser.setCurPage(currentUser.getCurPage() + 1);
        } else if (currentUser.getCurPage() > currentUser.getTotalPages()) {
            currentUser.setCurPage(currentUser.getTotalPages());
        } else {
            // Текущая страница НЕ изменилось, просто возвращаемся в список пользователей
            return "redirect:/settings/users";
        }

        // Текущая страница пользователя изменилась, сохраняем текущего пользователя
        userService.saveUser(currentUser);

        // Переходим в список пользователей
        return "redirect:/settings/users";
    }

    @PostMapping("/users/change-page-size")
    public String changePageSizeUsers(@ModelAttribute("currentUser") MyUser user,
                                      @AuthenticationPrincipal MyUser currentUser) {
        currentUser.setPageSize(user.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/settings/users";
    }

    private long calculateTotalElements() {
        return userService.usersCount();
    }

    private int calculateTotalPages(long totalElements, int pageSize) {
        if (totalElements % pageSize == 0) {
            return (int) totalElements / pageSize;
        }
        return (int) totalElements / pageSize + 1;
    }

    private boolean isChangedUserProperties(MyUser user) {
        // Флаг изменений в настройках пользователя
        boolean isChanged = false;

        // Вычисляем новые настройки пользователя:
        // - количество элементов в списке
        // - размер страницы в списке (если необходимо, сразу устанавливаем пользователю)
        // - количество страниц в списке
        // - текущую страницу
        long totalElements = calculateTotalElements();
        if (user.getPageSize() < 1) {
            user.setPageSize(1);
            isChanged = true;
        } else if (user.getPageSize() > 20) {
            user.setPageSize(20);
            isChanged = true;
        }
        int totalPages = calculateTotalPages(totalElements, user.getPageSize());
        if (user.getCurPage() < 1) {
            user.setCurPage(1);
            isChanged = true;
        } else if (user.getCurPage() > totalPages) {
            user.setCurPage(totalPages);
            isChanged = true;
        }

        // Проверяем, изменились ли настройки:
        // - количество элементов списка
        // - количество страниц
        // ЕСЛИ да, ТО устанавливаем пользователю новые настройки
        if ( user.getTotalElements() != totalElements ||
                user.getTotalPages() != totalPages ) {
            user.setTotalElements(totalElements);
            user.setTotalPages(totalPages);
            isChanged = true;
        }

        return isChanged;
    }

    // Методы управления списком пользователей и отдельными пользователями
    @GetMapping("/users")
    public String getUsers(Model model, @AuthenticationPrincipal MyUser currentUser) {
        // Обновляем настройки текущего пользователю
        // и сохраняем его, если настройки изменились
        if (isChangedUserProperties(currentUser)) {
            userService.saveUser(currentUser);
        }

        model.addAttribute("users", userService.users(
                currentUser.getCurPage() - 1, currentUser.getPageSize()));

        return "users-list";
    }

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("returnTo", "/settings/users");
        model.addAttribute("mode", "admin");
        return "user-profile";
    }

    @GetMapping("/users/{id}/change-password")
    public String openChangePasswordFormAdminMode(
            @PathVariable("id") Long id,
            Model model) {

        MyUser user = userService.getUserById(id);

        model.addAttribute("user",
                ChangePasswordForm.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .currentPass("*")   // формальное значение, чтобы избежать проверки на null значение
                        .role(user.getRole())
                        .email(user.getEmail())
                        .mode("admin")
                        .accountType(user.getAccountType())
                        .build());
        model.addAttribute("returnToWhenGet", "/settings/users");
        model.addAttribute("returnToWhenPost", "/settings/users/change-password");

        return "user-password-change";
    }

    @PostMapping("/users/change-password")
    public String changeUserPasswordAdminMode(
            Model model,
            @Valid @ModelAttribute("user") ChangePasswordForm form,
            BindingResult errors) {

        if ( !userService.isCorrectNewPassword(
                form.getId(),
                form,
                errors, model, form.getMode(), form.getAccountType()) ) {
            model.addAttribute("returnToWhenGet", "/settings/users");
            model.addAttribute("returnToWhenPost", "/settings/users/change-password");
            return "user-password-change";
        }

        MyUser user = userService.getUserById(form.getId());
        user.setPassword(userService.getEncodedPassword(form.getNewPassword()));
        if ("GOOGLE".equals(user.getAccountType())) {
            user.setAccountType("LOCAL");
        }

        userService.saveUser(user);

        return "redirect:/settings/users";
    }

    @GetMapping("/users/{id}/confirm-user-locking")
    public String userLockingConfirm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("action", "locking");
        model.addAttribute("actionUri", "/settings/users/user-locking");
        model.addAttribute("returnTo", "/settings/users");

        return "confirm-action-on-user";
    }


    @PostMapping("/users/user-locking")
    public String userLocking(@RequestParam("id") Long id,
                              @AuthenticationPrincipal MyUser currentUser,
                              Authentication authentication,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        String username = userService.getUserById(id).getUsername();

        if (userService.lockUser(id, currentUser)) {
            if (authentication != null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                return "redirect:/login?expired";
            }
        } else {
            sessionService.expireUserSessions(username);
        }

        return "redirect:/settings/users";
    }

    @GetMapping("/users/{id}/confirm-user-deleting")
    public String userDeleting(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("action", "deleting");
        model.addAttribute("actionUri", "/settings/users/user-deleting");
        model.addAttribute("returnTo", "/settings/users");

        return "confirm-action-on-user";
    }

    @PostMapping("/users/user-deleting")
    public String userDeleting(@RequestParam("id") Long id,
                              @AuthenticationPrincipal MyUser currentUser,
                              Authentication authentication,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        String username = userService.getUserById(id).getUsername();

        if (userService.deleteUser(id, currentUser)) {
            if (authentication != null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                return "redirect:/login?expired";
            }
        } else {
            sessionService.expireUserSessions(username);
        }

        return "redirect:/settings/users";
    }

}
