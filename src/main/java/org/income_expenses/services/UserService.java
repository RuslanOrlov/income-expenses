package org.income_expenses.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.dto.RegisterForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.MyUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    final MyUserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    public List<MyUser> users() {
        return userRepository.findAll(Sort.by("id"));
    }

    public List<MyUser> users(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return userRepository.findAll(pageable).getContent();
    }

    public long usersCount() {
        return userRepository.count();
    }

    public void saveUser(MyUser user) {
        userRepository.save(user);
    }

    public MyUser getUserById(Long id) {
        Optional<MyUser> user = userRepository.findById(id);
        return user.orElseThrow(() -> new NoSuchElementException("No user found"));
    }

    public boolean checkUserExists(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email).isPresent();
    }

    public boolean isCorrectNewPassword(Long id,
                                        ChangePasswordForm form,
                                        BindingResult errors,
                                        Model model, String mode, String accountType) {
        MyUser myUser = getUserById(id);

        if (  ("admin".equals(mode) || "GOOGLE".equals(accountType)) &&
                (errors.hasErrors() || !form.isConfirmEqualsNewPassword())  ) {
            // Новый пароль и подтверждение пароля не совпадают
            if (!form.isConfirmEqualsNewPassword())
                model.addAttribute("confirmNewPasswordError", true);
            return false;
        }

        if ( mode.equals("user") && "LOCAL".equals(accountType) &&
                ( errors.hasErrors() ||
                        !form.isConfirmEqualsNewPassword() ||
                        ( !"*".equals(form.getCurrentPass()) &&
                                !passwordEncoder.matches(form.getCurrentPass(), myUser.getPassword())
                        ) ||
                        passwordEncoder.matches(form.getNewPassword(),  myUser.getPassword())
                )
           ) {

            // Новый пароль и подтверждение пароля не совпадают
            if (!form.isConfirmEqualsNewPassword())
                model.addAttribute("confirmNewPasswordError", true);

            log.info("--- form.getCurrentPass() = {}", form.getCurrentPass());
            // Текущий пароль указан неправильно
            if (!"*".equals(form.getCurrentPass())) {
                // Если currentPass == "*", значит пользователь был создан с помощью
                // Google регистрации, и тогда ниже следующая проверка НЕ нужна
                if (!passwordEncoder.matches(form.getCurrentPass(), myUser.getPassword())) {
                    model.addAttribute("currentPasswordError", true);
                    log.info("--- Текущий пароль указан неправильно - не совпадает");
                }
            }

            // Новый пароль совпадает с теукщим паролем
            if (passwordEncoder.matches(form.getNewPassword(),  myUser.getPassword()))
                model.addAttribute("newPasswordIsNotDifferentError", true);

            return false;
        };
        return true;
    }

    public String getEncodedPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }

    public boolean isMatches(String rawPass, String encodedPass) {
        return passwordEncoder.matches(rawPass, encodedPass);
    }

    public boolean isCorrectNewUser(RegisterForm user,
                                    BindingResult errors,
                                    Model model) {
        boolean isCorrect = true;

        if (errors.hasErrors()) {
            isCorrect = false;
            List<FieldError> fieldErrors = errors.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                if (fieldError.getField().equals("password"))
                    model.addAttribute("errorEmptyPass", true);
                if (fieldError.getField().equals("username"))
                    model.addAttribute("errorEmptyName", true);
                if (fieldError.getField().equals("role"))
                    model.addAttribute("errorRole", true);
                if (fieldError.getField().equals("email"))
                    model.addAttribute("errorEmail", true);
            }
        }

        if (!user.isConfirmEqualsPassword()) {
            isCorrect = false;
            model.addAttribute("errorMismatchPass", true);
        }

        if (checkUserExists(user.getUsername(), user.getEmail())) {
            isCorrect = false;
            model.addAttribute("errorUser", true);
        }

        return isCorrect;
    }

    public boolean lockUser(Long id, MyUser currentUser) {
        MyUser user = getUserById(id);

        user.setAccountNonLocked(!user.isAccountNonLocked());

        saveUser(user);

        return id == currentUser.getId();
    }

    public boolean deleteUser(Long id, MyUser currentUser) {
        userRepository.deleteById(id);
        return id == currentUser.getId();
    }
}
