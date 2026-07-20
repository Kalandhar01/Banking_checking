package com.cib.customer.entity;

import com.cib.customer.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "corporate_customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String companyCode;

    @Column(nullable = false)
    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Builder.Default
    @OneToMany(
            mappedBy = "corporateCustomer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CorporateUser> users = new ArrayList<>();
}
