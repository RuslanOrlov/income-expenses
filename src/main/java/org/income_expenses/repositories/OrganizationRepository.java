package org.income_expenses.repositories;

import org.income_expenses.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
