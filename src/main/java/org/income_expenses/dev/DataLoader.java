package org.income_expenses.dev;

import lombok.RequiredArgsConstructor;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        repo.save(MyUser.builder()
                .username("user1")
                .password(passwordEncoder.encode("1"))
                .role("USER")
                .build());
        repo.save(MyUser.builder()
                .username("user2")
                .password(passwordEncoder.encode("2"))
                .role("USER")
                .build());
        repo.save(MyUser.builder()
                .username("admin1")
                .password(passwordEncoder.encode("1"))
                .role("ADMIN")
                .build());
    }
}


