package org.income_expenses.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.OrganizationDto;
import org.income_expenses.dto.TransactionTypeDto;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.income_expenses.services.DirectoryOfPersonalFinanceService;
import org.income_expenses.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    /* Методы управления справочником TransactionType (Типы транзакций) */
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
    public String getTransactionTypeById(@PathVariable Long id, Model model,
                                         @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        model.addAttribute("transactionType", directoryService.getTransactionTypeById(id));
        model.addAttribute("curPage", curPage);
        model.addAttribute("mainPath", "/finance/directories/tranasction-types");
        model.addAttribute("title", "Просмотр карточки типа транзакции");
        return "directory-transaction-type-card";
    }

    @GetMapping("/tranasction-types/create")
    public String openCreationTransactionTypeForm(Model model) {
        model.addAttribute("transactionType", new TransactionTypeDto());
        model.addAttribute("categories", TransactionCategory.values());
        model.addAttribute("mainPath", "/finance/directories/tranasction-types");
        model.addAttribute("title", "Создание нового типа транзакции");
        return "directory-transaction-type-create";
    }

    @PostMapping("/tranasction-types/create")
    public String createTransactionType(@ModelAttribute("transactionType") @Valid TransactionTypeDto transactionType,
                                        BindingResult bindingResult, Model model) {
        if (directoryService.isTransactionTypeExists(transactionType, bindingResult)) {
            bindingResult.reject("transactionType.exists",
                    "Тип транзакции с таким же наименованием и категорией уже существует");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("transactionType", transactionType);
            model.addAttribute("categories", TransactionCategory.values());
            model.addAttribute("mainPath", "/finance/directories/tranasction-types");
            model.addAttribute("title", "Создание нового типа транзакции");
            return "directory-transaction-type-create";
        }
        directoryService.createTransactionType(transactionType);
        return "redirect:/finance/directories/tranasction-types";
    }

    @GetMapping("/tranasction-types/{id:\\d+}/change")
    public String openEditTransactionTypeForm(@PathVariable Long id, Model model,
                                              @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        TransactionTypeDto transactionType = directoryService.getTransactionTypeById(id).toDto();

        model.addAttribute("transactionType", transactionType);
        model.addAttribute("categories", TransactionCategory.values());
        model.addAttribute("mainPath", "/finance/directories/tranasction-types");
        model.addAttribute("title", "Изменение типа транзакции");
        model.addAttribute("curPage", curPage);
        model.addAttribute("id", id);

        return "directory-transaction-type-change";
    }

    @PostMapping("/tranasction-types/{id:\\d+}/change")
    public String editTransactionType(@PathVariable Long id,
                                      @Valid @ModelAttribute("transactionType") TransactionTypeDto transactionType,
                                      BindingResult bindingResult, Model model,
                                      @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        if (directoryService.isUpdatedTransactionTypeExists(transactionType, bindingResult, id)) {
            bindingResult.reject("transactionType.exists",
                    "Тип транзакции с таким же наименованием и категорией уже существует");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("transactionType", transactionType);
            model.addAttribute("categories", TransactionCategory.values());
            model.addAttribute("mainPath", "/finance/directories/tranasction-types");
            model.addAttribute("title", "Изменение типа транзакции");
            model.addAttribute("curPage", curPage);
            model.addAttribute("id", id);
            return "directory-transaction-type-change";
        }
        directoryService.editTransactionType(id, transactionType);
        return "redirect:/finance/directories/tranasction-types" + "?curPage=" + curPage;
    }

    @GetMapping("/tranasction-types/{id:\\d+}/confirm-transaction-type-deleting")
    public String deletingTransactionTypeConfirm(
            @PathVariable("id") Long id, Model model,
            @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        model.addAttribute("directory", "\"Типы транзакций\"");
        model.addAttribute("action", "deleting");
        model.addAttribute("actionUri", "/finance/directories/tranasction-types/" + id + "/delete");
        model.addAttribute("returnTo", "/finance/directories/tranasction-types");
        model.addAttribute("curPage", curPage);
        model.addAttribute("id", null);
        model.addAttribute("selectedWalletId", null);

        return "confirm-action-on-directory";
    }

    @GetMapping("/tranasction-types/{id:\\d+}/delete")
    public String deleteTransactionType(@PathVariable("id") Long id,
                                    @RequestParam(value = "curPage", defaultValue = "0") int curPage, Model model) {
        if (directoryService.isTransactionTypeAlreadyUsed(id)) {
            model.addAttribute("directory", "\"Типы транзакций\"");
            model.addAttribute("action", "warning");
            model.addAttribute("actionUri", "/finance/directories/tranasction-types");
            model.addAttribute("returnTo", "/finance/directories/tranasction-types");
            model.addAttribute("curPage", curPage);
            model.addAttribute("id", id);
            model.addAttribute("selectedWalletId", null);
            return "confirm-action-on-directory";
        }
        directoryService.deleteTransactionType(id);
        return "redirect:/finance/directories/tranasction-types" + "?curPage=" + curPage;
    }

    /*  Методы управления справочником Organization
        (Организации (источники) поступления (траты) денег) */
    @GetMapping("/organizations")
    public String getAllOrganizations(Model model,
                                      @AuthenticationPrincipal MyUser currentUser,
                                      @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        Page<Organization> page = directoryService.getAllOrganizations(curPage, currentUser.getPageSize());
        model.addAttribute("organizations", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("curPage", curPage);
        model.addAttribute("mainPath", "/finance/directories/organizations");
        model.addAttribute("title", "Список организаций / источников");
        model.addAttribute("predefinedPageSizeValues", List.of(1,2,3,5,7,10,20));

        return "directory-organizations-list";
    }

    @GetMapping("/organizations/{id:\\d+}")
    public String getOrganizationById(@PathVariable Long id, Model model,
                                         @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        model.addAttribute("organization", directoryService.getOrganizationById(id));
        model.addAttribute("curPage", curPage);
        model.addAttribute("mainPath", "/finance/directories/organizations");
        model.addAttribute("title", "Просмотр карточки организации / источника");
        return "directory-organization-card";
    }

    @GetMapping("/organizations/create")
    public String openCreationOrganizationForm(Model model) {
        model.addAttribute("organization", new OrganizationDto());
        model.addAttribute("categories", TransactionCategory.values());
        model.addAttribute("mainPath", "/finance/directories/organizations");
        model.addAttribute("title", "Создание новой организации / источника");
        return "directory-organization-create";
    }

    @PostMapping("/organizations/create")
    public String createOrganization(@ModelAttribute("organization") @Valid OrganizationDto organization,
                                     BindingResult bindingResult, Model model) {
        if (directoryService.isOrganizationExists(organization, bindingResult)) {
            bindingResult.reject("organization.exists",
                    "Организация / источник с таким же наименованием и категорией уже существует");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("organization", organization);
            model.addAttribute("categories", TransactionCategory.values());
            model.addAttribute("mainPath", "/finance/directories/organizations");
            model.addAttribute("title", "Создание новой организации / источника");
            return "directory-organization-create";
        }
        directoryService.createOrganization(organization);
        return "redirect:/finance/directories/organizations";
    }

    @GetMapping("/organizations/{id:\\d+}/change")
    public String openEditOrganizationForm(@PathVariable Long id, Model model,
                                           @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        OrganizationDto organization = directoryService.getOrganizationById(id).toDto();

        model.addAttribute("organization", organization);
        model.addAttribute("categories", TransactionCategory.values());
        model.addAttribute("mainPath", "/finance/directories/organizations");
        model.addAttribute("title", "Изменение организации / источника");
        model.addAttribute("curPage", curPage);
        model.addAttribute("id", id);

        return "directory-organization-change";
    }

    @PostMapping("/organizations/{id:\\d+}/change")
    public String editOrganization(@PathVariable Long id,
                                   @Valid @ModelAttribute("organization") OrganizationDto organization,
                                   BindingResult bindingResult, Model model,
                                   @RequestParam(value = "curPage", defaultValue = "0") int curPage) {
        if (directoryService.isUpdatedOrganizationExists(organization, bindingResult, id)) {
            bindingResult.reject("organization.exists",
                    "Организация / источник с таким же наименованием и категорией уже существует");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("organization", organization);
            model.addAttribute("categories", TransactionCategory.values());
            model.addAttribute("mainPath", "/finance/directories/organizations");
            model.addAttribute("title", "Изменение организации / источника");
            model.addAttribute("curPage", curPage);
            model.addAttribute("id", id);
            return "directory-organization-change";
        }
        directoryService.editOrganization(id, organization);
        return "redirect:/finance/directories/organizations" + "?curPage=" + curPage;
    }

    @GetMapping("/organizations/{id:\\d+}/confirm-organization-deleting")
    public String deletingOrganizationConfirm(
            @PathVariable("id") Long id, Model model,
            @RequestParam(value = "curPage", defaultValue = "0") int curPage) {

        model.addAttribute("directory", "\"Организации / источники\"");
        model.addAttribute("action", "deleting");
        model.addAttribute("actionUri", "/finance/directories/organizations/" + id + "/delete");
        model.addAttribute("returnTo", "/finance/directories/organizations");
        model.addAttribute("curPage", curPage);
        model.addAttribute("id", null);
        model.addAttribute("selectedWalletId", null);

        return "confirm-action-on-directory";
    }

    @GetMapping("/organizations/{id:\\d+}/delete")
    public String deleteOrganization(@PathVariable("id") Long id,
                                     @RequestParam(value = "curPage", defaultValue = "0") int curPage, Model model) {
        if (directoryService.isOrganizationAlreadyUsed(id)) {
            model.addAttribute("directory", "\"Организации / источники\"");
            model.addAttribute("action", "warning");
            model.addAttribute("actionUri", "/finance/directories/organizations");
            model.addAttribute("returnTo", "/finance/directories/organizations");
            model.addAttribute("curPage", curPage);
            model.addAttribute("id", id);
            model.addAttribute("selectedWalletId", null);
            return "confirm-action-on-directory";
        }
        directoryService.deleteOrganization(id);
        return "redirect:/finance/directories/organizations" + "?curPage=" + curPage;
    }
}
