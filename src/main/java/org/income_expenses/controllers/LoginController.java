package org.income_expenses.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final List<LogoutHandler> handlers;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout-confirm")
    public String logoutConfirm(Model model, HttpServletRequest request) {
        String returnUri = request.getHeader("Referer");
        if (returnUri == null) {
            returnUri = "/";
        }

        log.info("\n\n");
        log.info("returnUri = {}", returnUri);
        log.info("\n\n");

        model.addAttribute("returnUri", returnUri);
        return "logout-confirm";
    }

    /* Так (см. ниже) тоже можно реализовать logout из системы, ЕСЛИ в SecurityConfig
       при настройке бина SecurityFilterChain задать http.csrf(csrf -> csrf.disable()),
       НО это будет неправильно с точки зрения безопасности Spring Security, потому что
       logout из системы является операцией, изменяющей состояние системы, и для нее
       должен использоваться POST запрос, который проверяется с помощью CSRF-токена. */

//    @GetMapping("/logout")
//    public String logout(Authentication authentication,
//                         HttpServletRequest request,
//                         HttpServletResponse response) {
//        handlers.forEach(handler ->
//                handler.logout(request, response, authentication));
//        return "redirect:/";
//    }
}
