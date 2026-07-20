package com.cib.customer.entity;


import com.cib.customer.enums.CheckerLevel;
import com.cib.customer.enums.Role;
import com.cib.customer.enums.Status;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "corporate_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private CheckerLevel checkerLevel;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CorporateCustomer corporateCustomer;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private CorporateAccount corporateAccount;
}
