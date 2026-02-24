package org.income_expenses.services;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<MyUser> users() {
        return userRepository.findAll();
    }

    public void saveUser(MyUser user) {
        userRepository.save(user);
    }

    public MyUser getUserById(Long id) {
        Optional<MyUser> user = userRepository.findById(id);
        return user.orElseThrow(() -> new NoSuchElementException("No user found"));
    }

    public boolean checkUserExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

}
