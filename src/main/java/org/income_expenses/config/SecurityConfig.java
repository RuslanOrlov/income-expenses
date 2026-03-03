package org.income_expenses.config;

import org.income_expenses.models.MyUser;
import org.income_expenses.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

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
                .formLogin(login -> login
                        .loginPage("/login"))
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
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**");
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            MyUser myUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return myUser;
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
