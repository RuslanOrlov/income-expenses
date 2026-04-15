package org.income_expenses.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.dto.ChangeSettingsForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.services.SessionService;
import org.income_expenses.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/user-profile")
    public String getUserProfile(Model model, @AuthenticationPrincipal MyUser user) {
        model.addAttribute("user", user);
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

    @GetMapping("/change-my-settings")
    public String mySettings(Model model,
                             @AuthenticationPrincipal MyUser user) {
        ChangeSettingsForm form = ChangeSettingsForm.builder()
                .id(user.getId())
                .username(user.getUsername())
                .pageSize(user.getPageSize())
                .build();
        model.addAttribute("user", form);
        return "user-settings-change";
    }

    @PostMapping("/change-my-settings")
    public String mySettings(@Valid @ModelAttribute("user") ChangeSettingsForm form,
                             BindingResult errors,
                             Model model,
                             @AuthenticationPrincipal MyUser currentUser) {
        if (errors.hasErrors()) {
            model.addAttribute("user", form);
            return "user-settings-change";
        }
        currentUser.setPageSize(form.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/settings";
    }

    // Метод управления размером страницы текущего пользователя
    @PostMapping("/users/change-page-size")
    public String changePageSizeUsers(@ModelAttribute("currentUser") MyUser user,
                                      @AuthenticationPrincipal MyUser currentUser) {
        currentUser.setPageSize(user.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/settings/users";
    }

    // Методы управления списком пользователей и отдельными пользователями
    @GetMapping("/users")
    public String getUsers(Model model,
                           @AuthenticationPrincipal MyUser currentUser,
                           @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        Page<MyUser> page = userService.users(
                curPage, currentUser.getPageSize());
        model.addAttribute("users", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("predefinedPageSizeValues", List.of(1,2,3,5,7,10,20));

        return "users-list";
    }

    @GetMapping("/users/{id}")
    public String getUserById(@PathVariable("id") Long id, Model model,
                              @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("returnTo", "/settings/users");
        model.addAttribute("mode", "admin");
        model.addAttribute("curPage", curPage);

        return "user-profile";
    }

    @GetMapping("/users/{id}/change-password")
    public String openChangePasswordFormAdminMode(
            @PathVariable("id") Long id,
            Model model,
            @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

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
        model.addAttribute("curPage", curPage);

        return "user-password-change";
    }

    @PostMapping("/users/change-password")
    public String changeUserPasswordAdminMode(
            Model model,
            @Valid @ModelAttribute("user") ChangePasswordForm form,
            BindingResult errors,
            @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        if ( !userService.isCorrectNewPassword(
                form.getId(),
                form,
                errors, model, form.getMode(), form.getAccountType()) ) {
            model.addAttribute("returnToWhenGet", "/settings/users");
            model.addAttribute("returnToWhenPost", "/settings/users/change-password");
            model.addAttribute("curPage", curPage);
            return "user-password-change";
        }

        MyUser user = userService.getUserById(form.getId());
        user.setPassword(userService.getEncodedPassword(form.getNewPassword()));
        if ("GOOGLE".equals(user.getAccountType())) {
            user.setAccountType("LOCAL");
        }

        userService.saveUser(user);

        return "redirect:/settings/users" + "?curPage=" + curPage;
    }

    @GetMapping("/users/{id}/confirm-user-locking")
    public String userLockingConfirm(@PathVariable("id") Long id, Model model,
                                     @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("action", "locking");
        model.addAttribute("actionUri", "/settings/users/user-locking");
        model.addAttribute("returnTo", "/settings/users");
        model.addAttribute("curPage", curPage);

        return "confirm-action-on-user";
    }

    @GetMapping("/users/user-locking")
    public String userLocking(@RequestParam("id") Long id,
                              @AuthenticationPrincipal MyUser currentUser,
                              Authentication authentication,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        String username = userService.getUserById(id).getUsername();

        if (userService.lockUser(id, currentUser)) {
            if (authentication != null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                return "redirect:/login?expired";
            }
        } else {
            sessionService.expireUserSessions(username);
        }

        return "redirect:/settings/users" + "?curPage=" + request.getParameter("curPage");
    }

    @GetMapping("/users/{id}/confirm-user-deleting")
    public String userDeleting(@PathVariable("id") Long id, Model model,
                               @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("action", "deleting");
        model.addAttribute("actionUri", "/settings/users/user-deleting");
        model.addAttribute("returnTo", "/settings/users");
        model.addAttribute("curPage", curPage);

        return "confirm-action-on-user";
    }

    @GetMapping("/users/user-deleting")
    public String userDeleting(@RequestParam("id") Long id,
                               @AuthenticationPrincipal MyUser currentUser,
                               Authentication authentication,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        String username = userService.getUserById(id).getUsername();

        if (userService.deleteUser(id, currentUser)) {
            if (authentication != null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                return "redirect:/login?expired";
            }
        } else {
            sessionService.expireUserSessions(username);
        }

        return "redirect:/settings/users" + "?curPage=" + request.getParameter("curPage");
    }

}
