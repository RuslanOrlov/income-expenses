package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class MyUser implements UserDetails {

    // Уникальный идентификатор пользователя
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Поля, описывающие пользователя
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "role", nullable = false)
    private String role;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked;

    // Поля управления постраничным просмотром данных пользователем
    @Column(name = "cur_page", nullable = false)
    private int curPage;
    @Column(name = "page_size", nullable = false)
    private int pageSize;
    @Column(name = "total_pages", nullable = false)
    private int totalPages;
    @Column(name = "total_elements", nullable = false)
    private long totalElements;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String[] roles = role != null ? role.split(";") : new String[0];
        return  Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
