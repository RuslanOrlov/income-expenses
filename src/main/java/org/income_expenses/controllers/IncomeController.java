package org.income_expenses.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.WalletTransaction;
import org.income_expenses.services.FinanceService;
import org.income_expenses.services.IncomeService;
import org.income_expenses.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/finance/income")
public class IncomeController {

    private final IncomeService incomeService;
    private final FinanceService financeService;
    private final UserService userService;

    @ModelAttribute("currentUser")
    public MyUser currentUser(@AuthenticationPrincipal MyUser user) {
        return user;
    }

    @ModelAttribute("totalElementsList")
    public long totalElements(@AuthenticationPrincipal MyUser currentUser) {
        return calculateTotalElements(currentUser);
    }

    // Методы управления постраничным просмотром списка пользователей
    @GetMapping("/prev")
    public String prevPageUsers(@AuthenticationPrincipal MyUser currentUser) {
        // Изменяем текущую страницу И сохраняем текущего пользователя
        if (currentUser.getCurPage() > 1) {
            currentUser.setCurPage(currentUser.getCurPage() - 1);
            userService.saveUser(currentUser);
        }

        // Переходим в список пользователей
        return "redirect:/finance/income";
    }

    @GetMapping("/next")
    public String nextPageUsers(@AuthenticationPrincipal MyUser currentUser) {
        // Изменяем текущую страницу пользователя
        if (currentUser.getCurPage() < currentUser.getTotalPages()) {
            currentUser.setCurPage(currentUser.getCurPage() + 1);
        } else if (currentUser.getCurPage() > currentUser.getTotalPages()) {
            currentUser.setCurPage(currentUser.getTotalPages());
        } else {
            // Текущая страница НЕ изменилось, просто возвращаемся в список пользователей
            return "redirect:/finance/income";
        }

        // Текущая страница пользователя изменилась, сохраняем текущего пользователя
        userService.saveUser(currentUser);

        // Переходим в список пользователей
        return "redirect:/finance/income";
    }

    @PostMapping("/change-page-size")
    public String changePageSizeUsers(@ModelAttribute("currentUser") MyUser user,
                                      @AuthenticationPrincipal MyUser currentUser) {
        currentUser.setPageSize(user.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/finance/income";
    }

    private long calculateTotalElements(MyUser currentUser) {
        FamilyWallet wallet = financeService.findWalletByOwner(currentUser);
        return incomeService.incomeTransactionsCount(currentUser, wallet);
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
        long totalElements = calculateTotalElements(user);
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

    // Методы управления списком приходных транакций
    @GetMapping
    public String getIncomeTransactions(Model model, @AuthenticationPrincipal MyUser currentUser) {
        // Обновляем настройки текущего пользователю
        // и сохраняем его, если настройки изменились
        if (isChangedUserProperties(currentUser)) {
            userService.saveUser(currentUser);
        }

        FamilyWallet wallet = financeService.findWalletByOwner(currentUser);
        List<WalletTransaction> transactions = incomeService.getIncomeTransactions(
                currentUser, wallet,
                currentUser.getCurPage() > 0 ? currentUser.getCurPage() - 1 : currentUser.getCurPage(),
                currentUser.getPageSize());
        model.addAttribute("transactions", transactions);
        return "transactions-list";
    }

    @GetMapping("/{id:\\d+}")
    public String getIncomeTransactionCard(@PathVariable("id") Long id, Model model) {
        WalletTransaction income = incomeService.getIncomeCard(id);
        model.addAttribute("income", income);
        return "transaction-card";
    }

    @GetMapping("/create")
    public String openCreateForm(Model model) {
        model.addAttribute("types", incomeService.getIncomeTransactionTypeList());
        return "transaction-create";
    }

    @PostMapping("/create")
    public String createIncome(@Valid TransactionDto transaction,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal MyUser currentUser) {
        if (bindingResult.hasErrors()) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                log.info("--- error {}", error.getDefaultMessage());
            }
            return  "transaction-create";
        }

        incomeService.createIncomeTransaction(transaction, currentUser);

        return "redirect:/finance/income";
    }

    @GetMapping("/{id:\\d+}/confirm-transaction-deleting")
    public String userDeleting(@PathVariable("id") Long id, Model model) {
        WalletTransaction income = incomeService.getIncomeCard(id);

        model.addAttribute("user", income.getWhoPerformed().getUsername());
        model.addAttribute("action", "deleting");
        model.addAttribute("actionUri", "/finance/income/" + id + "/delete");
        model.addAttribute("returnTo", "/finance/income");

        return "confirm-action-on-transaction";
    }


    @GetMapping("/{id:\\d+}/delete")
    public String deleteIncome(@PathVariable("id") Long id) {
        incomeService.deleteIncome(id);
        return "redirect:/finance/income";
    }
}
