package com.cib.customer.repository;



import com.cib.customer.entity.CorporateUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporateUserRepository extends JpaRepository<CorporateUser, Long> {

    boolean existsByEmail(String email);

    Optional<CorporateUser> findByEmail(String email);
}
