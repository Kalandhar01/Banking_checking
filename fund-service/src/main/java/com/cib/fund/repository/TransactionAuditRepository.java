package com.cib.fund.repository;

import com.cib.fund.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
    List<TransactionAudit> findByTransactionIdOrderByCreatedAtAsc(Long transactionId);
}
