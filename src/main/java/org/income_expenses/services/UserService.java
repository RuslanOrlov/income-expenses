package org.income_expenses.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.income_expenses.dto.ChangePasswordForm;
import org.income_expenses.dto.RegisterForm;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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

    public boolean isCorrectNewPassword(Long id,
                                        ChangePasswordForm form,
                                        BindingResult errors,
                                        Model model) {
        MyUser myUser = getUserById(id);

        if (    errors.hasErrors() ||
                !form.isConfirmEqualsNewPassword() ||
                !passwordEncoder.matches(form.getCurrentPass(), myUser.getPassword()) ||
                passwordEncoder.matches(form.getNewPassword(),  myUser.getPassword()) ) {

            if (!form.isConfirmEqualsNewPassword())
                model.addAttribute("confirmNewPasswordError", true);

            if (!passwordEncoder.matches(form.getCurrentPass(), myUser.getPassword()))
                model.addAttribute("currentPasswordError", true);

            if (passwordEncoder.matches(form.getNewPassword(),  myUser.getPassword()))
                model.addAttribute("newPasswordIsNotDifferentError", true);
            return false;
        };

        myUser.setPassword(passwordEncoder.encode(form.getNewPassword()));

        saveUser(myUser);

        return true;
    }

    public boolean isCorrectNewUser(RegisterForm user,
                                    BindingResult errors,
                                    Model model) {
        if ( errors.hasErrors() ||
             !user.isConfirmEqualsPassword() ||
             checkUserExists(user.getUsername()) ) {

            if (errors.hasErrors()) {
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

            if (!user.isConfirmEqualsPassword())
                model.addAttribute("errorMismatchPass", true);

            if (checkUserExists(user.getUsername()))
                model.addAttribute("errorUser", true);

            return false;
        }

        saveUser(user.toUser(passwordEncoder));
        return true;
    }

    public boolean lockUser(Long id, MyUser currentUser) {
        MyUser user = getUserById(id);

        user.setAccountNonLocked(!user.isAccountNonLocked());

        saveUser(user);

        return id == currentUser.getId();
    }
}
