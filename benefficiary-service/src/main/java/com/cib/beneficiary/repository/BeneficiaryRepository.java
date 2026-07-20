package com.cib.beneficiary.repository;

import com.cib.beneficiary.entity.Beneficiary;
import com.cib.beneficiary.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerId(Long customerId);

    List<Beneficiary> findByCustomerIdAndStatus(Long customerId, Status status);

    Optional<Beneficiary> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);
}
