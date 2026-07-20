package com.cib.customer.repository;

import com.cib.customer.entity.CorporateAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateAccountRepository extends JpaRepository<CorporateAccount, Long> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<CorporateAccount> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM CorporateAccount a WHERE a.id = :id")
    Optional<CorporateAccount> findByIdWithLock(@Param("id") Long id);
}
