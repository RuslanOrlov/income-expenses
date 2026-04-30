package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.*;
import org.income_expenses.dto.TransactionTypeDto;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionTypeName;

    @Enumerated(value = EnumType.STRING)
    private TransactionCategory category;

    private LocalDateTime createdAt;

    public TransactionTypeDto toDto() {
        return TransactionTypeDto.builder()
                .id(id)
                .transactionTypeName(transactionTypeName)
                .category(category)
                .build();
    }
}
