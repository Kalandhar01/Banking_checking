package com.cib.approval.service.impl;

import com.cib.approval.dto.*;
import com.cib.approval.entity.ApprovalRequest;
import com.cib.approval.enums.ApprovalStatus;
import com.cib.approval.exception.InvalidApprovalException;
import com.cib.approval.exception.ResourceNotFoundException;
import com.cib.approval.feign.CustomerServiceClient;
import com.cib.approval.feign.FundServiceClient;
import com.cib.approval.mapper.ApprovalMapper;
import com.cib.approval.repository.ApprovalRequestRepository;
import com.cib.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepository approvalRepository;
    private final FundServiceClient fundServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional
    public ApprovalResponse submitForApproval(ApprovalRequestDto request) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(request.getTransactionId());
        if (transaction == null) {
            throw new ResourceNotFoundException(
                    "Transaction not found with ID: " + request.getTransactionId());
        }

        CustomerUserDto maker = customerServiceClient.getUser(Long.valueOf(request.getMakerId()));
        if (maker == null) {
            throw new ResourceNotFoundException("Maker not found with ID: " + request.getMakerId());
        }
        if (!"MAKER".equalsIgnoreCase(maker.getRole())) {
            throw new InvalidApprovalException(
                    "User " + request.getMakerId() + " is not a MAKER. Only MAKERs can initiate transactions.");
        }
        if (!"ACTIVE".equalsIgnoreCase(maker.getStatus())) {
            throw new InvalidApprovalException("Maker account is not ACTIVE");
        }

        CustomerUserDto checker = customerServiceClient.getUser(Long.valueOf(request.getCheckerId()));
        if (checker == null) {
            throw new ResourceNotFoundException("Checker not found with ID: " + request.getCheckerId());
        }
        if (!"CHECKER".equalsIgnoreCase(checker.getRole())) {
            throw new InvalidApprovalException(
                    "User " + request.getCheckerId() + " is not a CHECKER. Only CHECKERs can approve transactions.");
        }
        if (!"ACTIVE".equalsIgnoreCase(checker.getStatus())) {
            throw new InvalidApprovalException("Checker account is not ACTIVE");
        }

        if (approvalRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new InvalidApprovalException(
                    "Transaction " + request.getTransactionId() + " is already submitted for approval");
        }

        ApprovalRequest approval = ApprovalRequest.builder()
                .transactionId(request.getTransactionId())
                .makerId(request.getMakerId())
                .makerName(maker.getEmployeeName())
                .checkerId(request.getCheckerId())
                .status(ApprovalStatus.PENDING)
                .build();

        approval = approvalRepository.save(approval);
        log.info("Approval request created for transaction {} by maker {}", request.getTransactionId(), request.getMakerId());

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse approveTransaction(Long approvalId, String checkerId) {
        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with ID: " + approvalId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new InvalidApprovalException(
                    "Only PENDING approvals can be approved. Current status: " + approval.getStatus());
        }
        if (!approval.getCheckerId().equals(checkerId)) {
            throw new InvalidApprovalException(
                    "This approval request is assigned to checker " + approval.getCheckerId());
        }

        FundTransactionDto transaction = fundServiceClient.getTransaction(approval.getTransactionId());
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + approval.getTransactionId());
        }
        if (!"PENDING".equalsIgnoreCase(transaction.getStatus()) && !"MODIFIED".equalsIgnoreCase(transaction.getStatus())) {
            throw new InvalidApprovalException(
                    "Transaction is not in a state that can be approved. Current status: " + transaction.getStatus());
        }

        fundServiceClient.approveTransaction(approval.getTransactionId(), checkerId);

        approval.setStatus(ApprovalStatus.APPROVED);
        approval = approvalRepository.save(approval);

        log.info("Transaction {} approved by checker {}", approval.getTransactionId(), checkerId);
        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse rejectTransaction(Long approvalId, String checkerId, String reason) {
        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with ID: " + approvalId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new InvalidApprovalException(
                    "Only PENDING approvals can be rejected. Current status: " + approval.getStatus());
        }
        if (!approval.getCheckerId().equals(checkerId)) {
            throw new InvalidApprovalException(
                    "This approval request is assigned to checker " + approval.getCheckerId());
        }

        fundServiceClient.rejectTransaction(approval.getTransactionId(), checkerId, reason);

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setComments(reason);
        approval = approvalRepository.save(approval);

        log.info("Transaction {} rejected by checker {}. Reason: {}", approval.getTransactionId(), checkerId, reason);
        return approvalMapper.toResponse(approval);
    }

    @Override
    public List<ApprovalResponse> getPendingApprovals() {
        return approvalRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApprovalResponse> getApprovalsByChecker(String checkerId) {
        return approvalRepository.findByCheckerIdOrderByCreatedAtDesc(checkerId)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public ApprovalResponse getApprovalById(Long id) {
        ApprovalRequest approval = approvalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with ID: " + id));
        return approvalMapper.toResponse(approval);
    }

    @Override
    public FundTransactionDto getTransactionDetails(Long transactionId) {
        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + transactionId);
        }
        return transaction;
    }
}
