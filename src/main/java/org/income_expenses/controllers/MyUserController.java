package org.income_expenses.controllers;

import lombok.RequiredArgsConstructor;
import org.income_expenses.repositories.UserRepository;
import org.income_expenses.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/users")
@RequiredArgsConstructor
public class MyUserController {
    private final UserService userService;

    @GetMapping
    public String getUsers(Model model) {
        model.addAttribute("users", userService.users());
        return "users-list";
    }
}
