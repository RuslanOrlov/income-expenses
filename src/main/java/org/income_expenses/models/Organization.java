package org.income_expenses.models;

import jakarta.persistence.*;
import lombok.*;
import org.income_expenses.dto.OrganizationDto;
import org.income_expenses.dto.TransactionTypeDto;

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

    public OrganizationDto toDto() {
        return OrganizationDto.builder()
                .id(id)
                .organizationName(organizationName)
                .category(category)
                .build();
    }
}
