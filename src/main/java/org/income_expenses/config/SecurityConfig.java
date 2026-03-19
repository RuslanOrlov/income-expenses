package org.income_expenses.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.MyUserRepository;
import org.income_expenses.services.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    //private final PasswordEncoder passwordEncoder;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests( request -> request
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .maximumSessions(1)                 // Ограничение: 1 сессия на пользователя
                        .maxSessionsPreventsLogin(false)    // false: новый вход выбивает старый; true: запрет нового входа
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/login?expired")       // Указываем, куда отправить пользователя после вызова expireNow()
                )
                // Обычный вход по логину/паролю
                .formLogin(login -> login
                        .loginPage("/login"))
                // Вход через Google
                .oauth2Login(login -> login
                        // Используем ту же страницу для обеих кнопок входа
                        .loginPage("/login")
                        .authorizationEndpoint(authorization -> authorization
                                // Регистрируем кастомный резолвер здесь для передачи параметра action в Google и обратно
                                .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository)))
                        .userInfoEndpoint(userInfo -> userInfo
                                // Требуется для авторизации через Google пользователя из БД
                                .oidcUserService(customOidcUserService))
                        .failureHandler((request, response, exception) -> {
                            // Перенаправляем обратно на логин с текстом ошибки
                            request.getSession().setAttribute("error.message", exception.getMessage());
                            response.sendRedirect("/login?error=true");
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/"));
        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // Необходим для уведомления SessionRegistry об удалении сессий (через таймаут или logout)
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
    }

    @Bean
    UserDetailsService userDetailsService(MyUserRepository userRepository) {
        return username -> {
            MyUser myUser = userRepository.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return myUser;
        };
    }

    /*@Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/

    // Чтобы параметр action из форм входа и регистрации сохранился при переходе на сервер
    // Google и вернулся обратно, нужно добавить его в authorizationRequestCustomizer
    @Bean
    public DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository repo) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

        // Копируем параметр 'action' из исходного запроса в сессию
        resolver.setAuthorizationRequestCustomizer(customizer -> {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                String action = request.getParameter("action");
                if (action != null) {
                    // Кладем в сессию: "login" или "register"
                    request.getSession().setAttribute("oauth_action", action);
                }
            }
        });
        return resolver;
    }

}
