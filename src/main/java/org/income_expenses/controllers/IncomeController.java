package org.income_expenses.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.TransactionDto;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.WalletMember;
import org.income_expenses.models.WalletTransaction;
import org.income_expenses.repositories.WalletMemberRepository;
import org.income_expenses.services.FinanceService;
import org.income_expenses.services.IncomeService;
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

    private final IncomeService incomeService;
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
                                      @AuthenticationPrincipal MyUser currentUser) {
        currentUser.setPageSize(user.getPageSize());
        userService.saveUser(currentUser);
        return "redirect:/finance/income";
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

            Page<WalletTransaction> page = incomeService.getIncomeTransactions(
                    currentUser, wallet, curPage, currentUser.getPageSize());

            model.addAttribute("transactions", page.getContent());
            model.addAttribute("page", page);
        } else {
            model.addAttribute("transactions", List.of());
            model.addAttribute("page", Page.empty());
        }
        model.addAttribute("selectedWalletId", walletId);

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
