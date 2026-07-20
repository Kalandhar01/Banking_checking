package com.cib.fund.entity;

import com.cib.fund.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus toStatus;

    @Column(nullable = false)
    private String changedBy;

    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
