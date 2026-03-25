package org.income_expenses.repositories;

import org.income_expenses.models.MyUser;
import org.income_expenses.models.WalletMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletMemberRepository extends JpaRepository<WalletMember, Long> {

    Optional<WalletMember> findByMember(MyUser member);

}
