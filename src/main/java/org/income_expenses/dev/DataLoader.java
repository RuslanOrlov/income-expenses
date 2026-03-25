package org.income_expenses.dev;

import lombok.RequiredArgsConstructor;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.MyUserRepository;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final MyUserRepository repo;
    private final TransactionTypeRepository transactionTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        repo.save(MyUser.builder()
                .username("user1")
                .password(passwordEncoder.encode("1"))
                .role("USER")
                .email("user1@google.com")
                .accountNonLocked(true)
                .originalAccountType("LOCAL")
                .accountType("LOCAL")
                .build());
        repo.save(MyUser.builder()
                .username("user2")
                .password(passwordEncoder.encode("2"))
                .role("USER")
                .email("user2@google.com")
                .accountNonLocked(true)
                .originalAccountType("LOCAL")
                .accountType("LOCAL")
                .build());
        repo.save(MyUser.builder()
                .username("admin1")
                .password(passwordEncoder.encode("1"))
                .role("ADMIN")
                .email("admin1@google.com")
                .accountNonLocked(true)
                .originalAccountType("LOCAL")
                .accountType("LOCAL")
                .build());
        transactionTypeRepository
                .save(TransactionType.builder()
                        .transactionTypeName("Текущий остаток денег")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
        transactionTypeRepository
                .save(TransactionType.builder()
                        .transactionTypeName("Пенсия / пособие")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
    }
}


