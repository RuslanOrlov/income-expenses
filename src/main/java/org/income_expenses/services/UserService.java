package org.income_expenses.services;

import lombok.RequiredArgsConstructor;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.dto.RegisterForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.UserRepository;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public boolean checkUserExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isCorrectNewPassword(Long id,
                                        ChangePasswordForm form,
                                        BindingResult errors,
                                        Model model, String mode) {
        MyUser myUser = getUserById(id);

        if ( mode.equals("admin") && (errors.hasErrors() || !form.isConfirmEqualsNewPassword()) ) {
            // Новый пароль и подтверждение пароля не совпадают
            if (!form.isConfirmEqualsNewPassword())
                model.addAttribute("confirmNewPasswordError", true);
            return false;
        }

        if ( mode.equals("user") &&
                ( errors.hasErrors() ||
                  !form.isConfirmEqualsNewPassword() ||
                  !passwordEncoder.matches(form.getCurrentPass(), myUser.getPassword()) ||
                  passwordEncoder.matches(form.getNewPassword(),  myUser.getPassword())
                )
           ) {

            // Новый пароль и подтверждение пароля не совпадают
            if (!form.isConfirmEqualsNewPassword())
                model.addAttribute("confirmNewPasswordError", true);

            // Текущий пароль указан неправильно
            if (!passwordEncoder.matches(form.getCurrentPass(), myUser.getPassword()))
                model.addAttribute("currentPasswordError", true);

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

        if (checkUserExists(user.getUsername())) {
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


}
