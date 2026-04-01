package org.income_expenses.services;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.MyUserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MyUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();

        ServletRequestAttributes attr = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpSession session = attr.getRequest().getSession();

        String action = (session != null) ? (String) session.getAttribute("oauth_action") : null;

        log.info("Current action = {}", action);

        Optional<MyUser> userOptional = userRepository.findByUsernameOrEmail(email, email);

        if (action.equals("login")) {
            if (!userOptional.isPresent()) {
                throw new UsernameNotFoundException("Пользователь с email '" + email + "' не найден!");
            }
            MyUser user = userOptional.get();
            user.setAttributes(oidcUser.getAttributes());
            user.setClaims(oidcUser.getClaims());
            user.setUserInfo(oidcUser.getUserInfo());
            user.setIdToken(oidcUser.getIdToken());

            log.info("User {} logged in with Google", user.getUsername());

            return user;
        }

        if (action.equals("register")) {
            if (userOptional.isPresent()) {
                throw new OAuth2AuthenticationException("Пользователь с email '" + email + "' уже существует!");
            }
            MyUser user = MyUser.builder()
                    .username(email)
                    .password("*") // ???
                    .role("USER")
                    .email(email)
                    .accountNonLocked(true)
                    .originalAccountType("GOOGLE")
                    .accountType("GOOGLE")
                    .pageSize(1)
                    .attributes(oidcUser.getAttributes())
                    .claims(oidcUser.getClaims())
                    .userInfo(oidcUser.getUserInfo())
                    .idToken(oidcUser.getIdToken())
                    .build();
            user = userRepository.save(user);

            log.info("User {} created and logged in with Google", user.getUsername());

            return user;
        }
        // При отсутствии параметра запроса action выбрасываем исключение
        throw new OAuth2AuthenticationException("При выполнении входа в приложении или регистрации пользователя через Google не был указан тип операции");
    }
}
