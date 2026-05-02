package org.income_expenses.repositories;

import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByOrganizationNameIgnoringCaseAndCategory(String organizationName, TransactionCategory category);

    boolean existsByOrganizationNameIgnoringCaseAndCategoryAndIdNot(String organizationName, TransactionCategory category, Long id);
}
