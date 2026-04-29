package org.income_expenses.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.TransactionType;
import org.income_expenses.services.DirectoryOfPersonalFinanceService;
import org.income_expenses.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/finance/directories")
public class DirectoryOfPersonalFinanceController {

    private final DirectoryOfPersonalFinanceService directoryService;
    private final UserService userService;

    @ModelAttribute("currentUser")
    public MyUser currentUser(@AuthenticationPrincipal MyUser currentUser) {
        return currentUser;
    }

    // Метод управления размером страницы пользователя
    @PostMapping("/change-page-size")
    public String changePageSizeUsers(@ModelAttribute("currentUser") MyUser user,
                                      @ModelAttribute("mainPath") String mainPath,
                                      @AuthenticationPrincipal MyUser currentUser) {
        currentUser.setPageSize(user.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:" + mainPath;
    }

    @GetMapping("/tranasction-types")
    public String getAllTransactionTypes(Model model,
                                         @AuthenticationPrincipal MyUser currentUser,
                                         @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        Page<TransactionType> page = directoryService.getAllTransactionTypes(curPage, currentUser.getPageSize());
        model.addAttribute("transactionTypes", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("curPage", curPage);
        model.addAttribute("mainPath", "/finance/directories/tranasction-types");
        model.addAttribute("title", "Список типов транзакций");
        model.addAttribute("predefinedPageSizeValues", List.of(1,2,3,5,7,10,20));

        return "directory-transaction-types-list";
    }

    @GetMapping("/tranasction-types/{id:\\d+}")
    public String getTransactionTypeById(@PathVariable Long id) {
        return "directory-transaction-type-card";
    }

    @GetMapping("/tranasction-types/create")
    public String openCreationTransactionTypeForm() {
        return "directory-transaction-type-create";
    }

    @PostMapping("/tranasction-types/create")
    public String createTransactionType() {
        return "redirect:/finance/directories";
    }

    @GetMapping("/tranasction-types/{id:\\d+}/change")
    public String openEditTransactionTypeForm(@PathVariable Long id) {
        return "directory-transaction-type-change";
    }

    @PostMapping("/tranasction-types/{id:\\d+}/change")
    public String editTransactionType(@PathVariable Long id) {
        return "redirect:/finance/directories";
    }
}
