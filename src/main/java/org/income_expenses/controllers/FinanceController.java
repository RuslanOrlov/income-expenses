package org.income_expenses.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.income_expenses.services.FinanceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/finance")
public class FinanceController {

    private final FinanceService financeService;

    @ModelAttribute("user")
    public MyUser currentUser(@AuthenticationPrincipal MyUser user) {
        return user;
    }

    @GetMapping
    public String financePage() {
        return "home-page-of-personal-finance";
    }


    @GetMapping("/wallet")
    public String wallet(Model model, @AuthenticationPrincipal MyUser currentUser) {
        if (financeService.existsWalletByOwner(currentUser)) {
            model.addAttribute("wallet", financeService.findWalletByOwner(currentUser));
            return "wallet-show";
        }
        return "offer-to-create-wallet";
    }

    @GetMapping("/wallet/create")
    public String openCreateForm(Model model, @AuthenticationPrincipal MyUser currentUser) {
        model.addAttribute("walletName", "Wallet of " + currentUser.getUsername());

        return "wallet-create";
    }

    @PostMapping("/wallet/create")
    public String createWallet(@RequestParam("walletName") String walletName,
                               @AuthenticationPrincipal MyUser currentUser) {
        financeService.createWallet(walletName, currentUser);

        return "redirect:/finance/wallet";
    }
}
