package org.income_expenses.dev;

import lombok.RequiredArgsConstructor;
import org.income_expenses.models.MyUser;
import org.income_expenses.models.Organization;
import org.income_expenses.models.TransactionCategory;
import org.income_expenses.models.TransactionType;
import org.income_expenses.repositories.MyUserRepository;
import org.income_expenses.repositories.OrganizationRepository;
import org.income_expenses.repositories.TransactionTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final MyUserRepository userRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        userRepository.save(MyUser.builder()
                        .username("user1")
                        .password(passwordEncoder.encode("1"))
                        .role("USER")
                        .email("user1@google.com")
                        .accountNonLocked(true)
                        .originalAccountType("LOCAL")
                        .accountType("LOCAL")
                        .pageSize(1)
                        .build());
        userRepository.save(MyUser.builder()
                        .username("user2")
                        .password(passwordEncoder.encode("2"))
                        .role("USER")
                        .email("user2@google.com")
                        .accountNonLocked(true)
                        .originalAccountType("LOCAL")
                        .accountType("LOCAL")
                        .pageSize(1)
                        .build());
        userRepository.save(MyUser.builder()
                        .username("admin1")
                        .password(passwordEncoder.encode("1"))
                        .role("ADMIN")
                        .email("admin1@google.com")
                        .accountNonLocked(true)
                        .originalAccountType("LOCAL")
                        .accountType("LOCAL")
                        .pageSize(1)
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
        transactionTypeRepository
                .save(TransactionType.builder()
                        .transactionTypeName("Зарплата")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
        transactionTypeRepository
                .save(TransactionType.builder()
                        .transactionTypeName("Покупка в магазине")
                        .category(TransactionCategory.EXPENSE)
                        .createdAt(LocalDateTime.now())
                        .build());
        transactionTypeRepository
                .save(TransactionType.builder()
                        .transactionTypeName("Оплата коммунальных услуг")
                        .category(TransactionCategory.EXPENSE)
                        .createdAt(LocalDateTime.now())
                        .build());
        transactionTypeRepository
                .save(TransactionType.builder()
                        .transactionTypeName("Оплата налогов и отчислений")
                        .category(TransactionCategory.EXPENSE)
                        .createdAt(LocalDateTime.now())
                        .build());

        organizationRepository
                .save(Organization.builder()
                        .organizationName("НАО Государственная корпорация \"Правительство для граждан\"")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
        organizationRepository
                .save(Organization.builder()
                        .organizationName("Собственные деньги")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
        organizationRepository
                .save(Organization.builder()
                        .organizationName("Работа самозанятым")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
        organizationRepository
                .save(Organization.builder()
                        .organizationName("Работа ИП")
                        .category(TransactionCategory.INCOME)
                        .createdAt(LocalDateTime.now())
                        .build());
        organizationRepository
                .save(Organization.builder()
                        .organizationName("Магазин")
                        .category(TransactionCategory.EXPENSE)
                        .createdAt(LocalDateTime.now())
                        .build());
        organizationRepository
                .save(Organization.builder()
                        .organizationName("Коммунальное предприятие")
                        .category(TransactionCategory.EXPENSE)
                        .createdAt(LocalDateTime.now())
                        .build());
        organizationRepository
                .save(Organization.builder()
                        .organizationName("Налоговый орган")
                        .category(TransactionCategory.EXPENSE)
                        .createdAt(LocalDateTime.now())
                        .build());
    }
}


