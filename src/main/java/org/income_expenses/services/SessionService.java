package org.income_expenses.services;

import lombok.RequiredArgsConstructor;
import org.income_expenses.models.MyUser;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRegistry sessionRegistry;

    public void expireUserSessions(String username) {
        // 1. Получаем список всех аутентифицированных пользователей
        List<Object> principals = sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {
            if (principal instanceof UserDetails userDetails && userDetails.getUsername().equals(username)) {
                // 2. Находим все сессии для этого пользователя
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

                // 3. Помечаем каждую сессию как истекшую
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        }
    }
}
