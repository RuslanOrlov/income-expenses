package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class MyUser implements UserDetails, OidcUser {

    // Уникальный идентификатор пользователя
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Поля, описывающие пользователя
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "role", nullable = false)
    private String role;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;
    @Column(name = "original_account_type", nullable = false)
    private String originalAccountType;
    @Column(name = "account_type", nullable = false)
    private String accountType;

    // Поля управления постраничным просмотром данных пользователем
    @Column(name = "cur_page", nullable = false)
    private int curPage;
    @Column(name = "page_size", nullable = false)
    private int pageSize;
    @Column(name = "total_pages", nullable = false)
    private int totalPages;
    @Column(name = "total_elements", nullable = false)
    private long totalElements;

    // Поля интрефейса OAuth2User и OidcUser, которые НЕ сохраняются в БД
    @Transient
    private Map<String, Object> attributes;
    @Transient
    private Map<String, Object> claims;
    @Transient
    private OidcUserInfo userInfo;
    @Transient
    private OidcIdToken idToken;

    // ========= UserDetails =========
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

    // ========= OAuth2User и OidcUser =========
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return getId().toString();
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}
