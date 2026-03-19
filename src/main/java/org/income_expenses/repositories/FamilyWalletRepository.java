package org.income_expenses.repositories;

import org.income_expenses.models.FamilyWallet;
import org.income_expenses.models.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyWalletRepository extends JpaRepository<FamilyWallet, Long> {

    Optional<FamilyWallet> findByOwner(MyUser owner);

    boolean existsByOwner(MyUser owner);

}
