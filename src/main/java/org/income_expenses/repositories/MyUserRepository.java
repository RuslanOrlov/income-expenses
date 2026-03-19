package org.income_expenses.repositories;

import org.income_expenses.models.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyUserRepository extends JpaRepository<MyUser, Long> {

    Optional<MyUser> findByUsername(String username);

    Optional<MyUser> findByEmail(String email);

    Optional<MyUser> findByUsernameOrEmail(String username, String email);
}
