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
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String organizationName;

    @Enumerated(value = EnumType.STRING)
    private TransactionCategory category;

    private LocalDateTime createdAt;
}
