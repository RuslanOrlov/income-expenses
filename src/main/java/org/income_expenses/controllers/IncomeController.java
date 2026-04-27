package org.income_expenses.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.WalletTransaction;
import org.income_expenses.repositories.WalletMemberRepository;
import org.income_expenses.services.FinanceService;
import org.income_expenses.services.IncomeExpenseService;
import org.income_expenses.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/finance/income")
public class IncomeController {

    private final IncomeExpenseService incomeExpenseService;
    private final FinanceService financeService;
    private final UserService userService;
    private final WalletMemberRepository walletMemberRepository;

    // Определение текущего пользователя как общего атрибута модели
    @ModelAttribute("currentUser")
    public MyUser currentUser(@AuthenticationPrincipal MyUser user) {
        user.setWallets(walletMemberRepository.findAllByMember(user));
        return user;
    }

    @ModelAttribute("userWallets")
    public List<FamilyWallet> wallets(@AuthenticationPrincipal MyUser user) {
        return walletMemberRepository.findAllByMember(user)
                .stream()
                .map(item -> item.getWallet())
                .collect(Collectors.toList());
    }

    // Метод управления размером страницы пользователя
    @PostMapping("/change-page-size")
    public String changePageSizeUsers(@ModelAttribute("currentUser") MyUser user,
                                      @AuthenticationPrincipal MyUser currentUser,
                                      @RequestParam(value = "walletId", required = false) Long walletId) {
        currentUser.setPageSize(user.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/finance/income" + (walletId != null ? "?walletId=" + walletId : "");
    }

    // Методы управления списком приходных транзакций
    @GetMapping
    public String getIncomeTransactions(Model model,
                                        @AuthenticationPrincipal MyUser currentUser,
                                        @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                                        @RequestParam(value = "walletId", required = false) Long walletId) {
        boolean walletSelected = (walletId != null);
        model.addAttribute("walletSelected", walletSelected);

        if (walletSelected) {
            FamilyWallet wallet = financeService.getFamilyWalletById(walletId);

            Page<WalletTransaction> page = incomeExpenseService.getIncomeTransactions(
                    currentUser, wallet, curPage, currentUser.getPageSize());

            model.addAttribute("transactions", page.getContent());
            model.addAttribute("page", page);
        } else {
            model.addAttribute("transactions", List.of());
            model.addAttribute("page", Page.empty());
        }

        model.addAttribute("mainPath", "/finance/income");
        model.addAttribute("title", "Список приходных транзакций");
        model.addAttribute("predefinedPageSizeValues", List.of(1,2,3,5,7,10,20));
        model.addAttribute("selectedWalletId", walletId);

        return "transactions-list";
    }

    @GetMapping("/{id:\\d+}")
    public String getTransactionCard(@PathVariable("id") Long id, Model model,
                                           @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                                           @RequestParam(value = "walletId", required = false) Long walletId) {
        WalletTransaction transaction = incomeExpenseService.getIncomeOrExpenseCard(id);

        model.addAttribute("mainPath", "/finance/income");
        model.addAttribute("title", "Просмотр приходной транзакции");
        model.addAttribute("transaction", transaction);
        model.addAttribute("selectedWalletId", walletId);
        model.addAttribute("curPage", curPage);

        return "transaction-card";
    }

    @GetMapping("/{id:\\d+}/change")
    public String openChangeForm(@PathVariable("id") Long id, Model model,
                                 @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                                 @RequestParam(value = "walletId", required = false) Long walletId) {
        WalletTransaction transaction = incomeExpenseService.getIncomeOrExpenseCard(id);

        model.addAttribute("mainPath", "/finance/income");
        model.addAttribute("title", "Редактирование приходной транзакции");
        model.addAttribute("transaction", transaction.toDto());
        model.addAttribute("organizations", incomeExpenseService.getOrganizations(TransactionCategory.INCOME));
        model.addAttribute("types", incomeExpenseService.getTransactionTypeList(TransactionCategory.INCOME));
        model.addAttribute("selectedWalletId", walletId);
        model.addAttribute("curPage", curPage);
        model.addAttribute("id", id);

        return "transaction-change";
    }

    @PostMapping("/{id:\\d+}/change")
    public String changeIncome(@PathVariable("id") Long id,
                               @ModelAttribute("transaction") @Valid TransactionDto transaction,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal MyUser currentUser,
                               @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                               @RequestParam(value = "walletId", required = false) Long walletId,
                               Model model) {
        if (incomeExpenseService.isWrongTransaction(transaction, bindingResult, "CHANGE")) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                log.info("--- error {}", error.getDefaultMessage());
            }
            model.addAttribute("mainPath", "/finance/income");
            model.addAttribute("title", "Редактирование приходной транзакции");
            model.addAttribute("transaction", transaction);
            model.addAttribute("organizations", incomeExpenseService.getOrganizations(TransactionCategory.INCOME));
            model.addAttribute("types", incomeExpenseService.getTransactionTypeList(TransactionCategory.INCOME));
            model.addAttribute("selectedWalletId", walletId);
            model.addAttribute("curPage", curPage);
            model.addAttribute("id", id);
            return "transaction-change";
        }

        incomeExpenseService.changeIncomeOrExpenseTransaction(transaction, id);

        return "redirect:/finance/income" + (walletId != null ? "?walletId=" + walletId + "&curPage=" + curPage : "");
    }

    @GetMapping("/create")
    public String openCreateForm(Model model,
                                 @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                                 @RequestParam(value = "walletId", required = false) Long walletId) {
        model.addAttribute("mainPath", "/finance/income");
        model.addAttribute("title", "Создание приходной транзакции");
        model.addAttribute("transaction", TransactionDto.builder()
                        .category(TransactionCategory.INCOME)
                        .items(List.of())
                        .build());
        model.addAttribute("organizations", incomeExpenseService.getOrganizations(TransactionCategory.INCOME));
        model.addAttribute("types", incomeExpenseService.getTransactionTypeList(TransactionCategory.INCOME));
        model.addAttribute("curPage", curPage);
        model.addAttribute("selectedWalletId", walletId);
        model.addAttribute("mode", "INCOME");
        return "transaction-create";
    }

    @PostMapping("/create")
    public String createIncome(@ModelAttribute("transaction") @Valid TransactionDto transaction,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal MyUser currentUser,
                               @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                               @RequestParam(value = "walletId", required = false) Long walletId,
                               Model model) {
        if (incomeExpenseService.isWrongTransaction(transaction, bindingResult, "CREATE")) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                log.info("--- error {}", error.getDefaultMessage());
            }
            model.addAttribute("mainPath", "/finance/income");
            model.addAttribute("title", "Создание приходной транзакции");
            model.addAttribute("transaction", transaction);
            model.addAttribute("organizations", incomeExpenseService.getOrganizations(TransactionCategory.INCOME));
            model.addAttribute("types", incomeExpenseService.getTransactionTypeList(TransactionCategory.INCOME));
            model.addAttribute("curPage", curPage);
            model.addAttribute("selectedWalletId", walletId);
            model.addAttribute("mode", "INCOME");
            return "transaction-create";
        }

        incomeExpenseService.createIncomeTransaction(transaction, currentUser);

        return "redirect:/finance/income" + (walletId != null ? "?walletId=" + walletId : "");
    }

    @GetMapping("/{id:\\d+}/confirm-transaction-deleting")
    public String deletingTransactionConfirm(@PathVariable("id") Long id, Model model,
                               @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                               @RequestParam(value = "walletId", required = false) Long walletId) {
        WalletTransaction transaction = incomeExpenseService.getIncomeOrExpenseCard(id);

        model.addAttribute("user", transaction.getWhoPerformed().getUsername());
        model.addAttribute("action", "deleting");
        model.addAttribute("actionUri", "/finance/income/" + id + "/delete");
        model.addAttribute("returnTo", "/finance/income");
        model.addAttribute("curPage", curPage);
        model.addAttribute("selectedWalletId", walletId);

        return "confirm-action-on-transaction";
    }

    @GetMapping("/{id:\\d+}/delete")
    public String deleteTransaction(@PathVariable("id") Long id,
                               @RequestParam(value = "curPage", defaultValue = "0") int curPage,
                               @RequestParam(value = "walletId", required = false) Long walletId) {
        incomeExpenseService.deleteIncomeOrExpense(id);
        return "redirect:/finance/income" + (walletId != null ? "?walletId=" + walletId + "&curPage=" + curPage : "");
    }
}
