package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Задается ограничение уникальности, гарантирующее, что комбинация
// полей wallet и member будет ункальной, то есть один пользователь
// сможет участвовать в одном семейном кошельке только один раз
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"wallet_id", "member_id"})
})
public class WalletMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private FamilyWallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private MyUser member;

    @Enumerated(value = EnumType.STRING)
    private WalletMemberRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private MyUser createdBy;

    private LocalDateTime createdAt;
}
