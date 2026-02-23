package org.income_expenses.controllers;

import lombok.RequiredArgsConstructor;
import org.income_expenses.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final UserService userService;

    @GetMapping
    public String getSettings() {
        return "settings";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.users());
        return "users-list";
    }
}
