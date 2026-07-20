package com.cib.approval.repository;

import com.cib.approval.entity.ApprovalRequest;
import com.cib.approval.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
    List<ApprovalRequest> findByStatusInOrderByCreatedAtDesc(List<ApprovalStatus> statuses);
    List<ApprovalRequest> findByCheckerIdOrLevel2CheckerIdOrderByCreatedAtDesc(String checkerId, String level2CheckerId);
    List<ApprovalRequest> findByCheckerIdIsNotNullOrderByCreatedAtDesc();
    List<ApprovalRequest> findByLevel2CheckerIdIsNotNullOrderByCreatedAtDesc();
    Optional<ApprovalRequest> findByTransactionId(Long transactionId);
}
