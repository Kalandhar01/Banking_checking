package com.cib.customer.repository;


import com.cib.customer.entity.CorporateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateCustomerRepository
        extends JpaRepository<CorporateCustomer, Long> {

    Optional<CorporateCustomer> findByCompanyCode(String companyCode);

    boolean existsByCompanyCode(String companyCode);
}
