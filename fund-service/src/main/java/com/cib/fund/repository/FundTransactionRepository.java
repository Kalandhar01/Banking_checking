package com.cib.fund.repository;

import com.cib.fund.entity.FundTransaction;
import com.cib.fund.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {
    List<FundTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<FundTransaction> findByReferenceNumber(String referenceNumber);
    List<FundTransaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
}
