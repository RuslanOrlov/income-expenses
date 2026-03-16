package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private MyUser owner;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletTransaction> transactions;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletMember> members;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<WalletHistory> historyRecords;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private MyUser createdBy;

    private LocalDateTime createdAt;
}
