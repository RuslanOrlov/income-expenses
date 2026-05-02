package org.income_expenses.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.income_expenses.dto.OrganizationDto;
import org.income_expenses.dto.TransactionTypeDto;
import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.OrganizationRepository;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.income_expenses.repositories.WalletTransactionRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DirectoryOfPersonalFinanceService {

    private final TransactionTypeRepository transactionTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public Page<TransactionType> getAllTransactionTypes(int curPage, int pageSize) {
        Pageable pageable = PageRequest.of(curPage, pageSize, Sort.by("id"));
        return transactionTypeRepository.findAll(pageable);
    }

    public TransactionType getTransactionTypeById(Long id) {
        return transactionTypeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaction type not found"));
    }

    public void createTransactionType(TransactionTypeDto transactionType) {
        TransactionType newTransactionType = TransactionType
                .builder()
                .transactionTypeName(transactionType.getTransactionTypeName())
                .category(transactionType.getCategory())
                .createdAt(LocalDateTime.now())
                .build();
        transactionTypeRepository.save(newTransactionType);
    }

    public void editTransactionType(Long id, TransactionTypeDto transactionType) {
        TransactionType updated = getTransactionTypeById(id);
        updated.setTransactionTypeName(transactionType.getTransactionTypeName());
        updated.setCategory(transactionType.getCategory());
        transactionTypeRepository.save(updated);
    }

    public void deleteTransactionType(Long id) {
        transactionTypeRepository.deleteById(id);
    }

    public boolean isTransactionTypeExists(TransactionTypeDto transactionType, BindingResult bindingResult) {
        String transactionTypeName = transactionType.getTransactionTypeName();
        TransactionCategory category = transactionType.getCategory();
        return transactionTypeRepository.existsByTransactionTypeNameIgnoringCaseAndCategory(transactionTypeName, category);
    }

    public boolean isUpdatedTransactionTypeExists(TransactionTypeDto transactionType, BindingResult bindingResult, Long id) {
        String transactionTypeName = transactionType.getTransactionTypeName();
        TransactionCategory category = transactionType.getCategory();
        return transactionTypeRepository.existsByTransactionTypeNameIgnoringCaseAndCategoryAndIdNot(transactionTypeName, category, id);
    }

    public boolean isTransactionTypeAlreadyUsed(Long id) {
        TransactionType transactionType = getTransactionTypeById(id);
        return walletTransactionRepository.existsByTransactionType(transactionType);
    }

    public Page<Organization> getAllOrganizations(int curPage, int pageSize) {
        Pageable pageable = PageRequest.of(curPage, pageSize, Sort.by("id"));
        return organizationRepository.findAll(pageable);
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    public void createOrganization(OrganizationDto organization) {
        Organization newOrganization = Organization
                .builder()
                .organizationName(organization.getOrganizationName())
                .category(organization.getCategory())
                .createdAt(LocalDateTime.now())
                .build();
        organizationRepository.save(newOrganization);
    }

    public void editOrganization(Long id, OrganizationDto organization) {
        Organization updated = getOrganizationById(id);
        updated.setOrganizationName(organization.getOrganizationName());
        updated.setCategory(organization.getCategory());
        organizationRepository.save(updated);
    }

    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }

    public boolean isOrganizationExists(OrganizationDto organization, BindingResult bindingResult) {
        String organizationName = organization.getOrganizationName();
        TransactionCategory category = organization.getCategory();
        return organizationRepository.existsByOrganizationNameIgnoringCaseAndCategory(organizationName, category);
    }

    public boolean isUpdatedOrganizationExists(OrganizationDto organization, BindingResult bindingResult, Long id) {
        String organizationName = organization.getOrganizationName();
        TransactionCategory category = organization.getCategory();
        return organizationRepository.existsByOrganizationNameIgnoringCaseAndCategoryAndIdNot(organizationName, category, id);
    }

    public boolean isOrganizationAlreadyUsed(Long id) {
        Organization organization = getOrganizationById(id);
        return walletTransactionRepository.existsByOrganization(organization);
    }
}
