package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.*;
import org.income_expenses.dto.TransactionItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id",referencedColumnName = "id")
    private WalletTransaction transaction;

    private LocalDateTime createdAt;

    public TransactionItemDto toDto() {
        TransactionItemDto transactionItemDto = TransactionItemDto.builder()
                .id(id)
                .amount(amount)
                .description(description)
                .build();
        return transactionItemDto;
    }
}
