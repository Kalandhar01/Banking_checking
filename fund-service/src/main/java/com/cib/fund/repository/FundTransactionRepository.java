package com.cib.fund.repository;

import com.cib.fund.entity.FundTransaction;
import com.cib.fund.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {
    List<FundTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<FundTransaction> findByReferenceNumber(String referenceNumber);

    List<FundTransaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);

    @Query("SELECT t FROM FundTransaction t WHERE t.status = 'PENDING' AND t.amount < :threshold ORDER BY t.createdAt DESC")
    List<FundTransaction> findPendingLevel1(BigDecimal threshold);

    @Query("SELECT t FROM FundTransaction t WHERE t.status = 'PENDING' AND t.amount >= :threshold ORDER BY t.createdAt DESC")
    List<FundTransaction> findPendingLevel2(BigDecimal threshold);

    @Query("SELECT t FROM FundTransaction t WHERE t.status = 'LEVEL1_APPROVED' ORDER BY t.createdAt DESC")
    List<FundTransaction> findLevel1Approved();
}
