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

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("100000");

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
                    "User " + request.getMakerId() + " is not a MAKER.");
        }
        if (!"ACTIVE".equalsIgnoreCase(maker.getStatus())) {
            throw new InvalidApprovalException("Maker account is not ACTIVE");
        }

        if (approvalRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new InvalidApprovalException(
                    "Transaction " + request.getTransactionId() + " is already submitted for approval");
        }

        createApproval(request.getTransactionId(), request.getMakerId(), maker.getEmployeeName(),
                transaction.getAmount());
        return approvalMapper.toResponse(approvalRepository
                .findByTransactionId(request.getTransactionId())
                .orElseThrow());
    }

    @Override
    @Transactional
    public ApprovalResponse approveTransactionByTxId(Long transactionId, String checkerId) {
        ApprovalRequest approval = approvalRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("No approval request found for transaction: " + transactionId));

        if (approval.getStatus() == ApprovalStatus.APPROVED
                || approval.getStatus() == ApprovalStatus.REJECTED) {
            throw new InvalidApprovalException(
                    "This transaction is already " + approval.getStatus());
        }

        CustomerUserDto checker = customerServiceClient.getUser(Long.valueOf(checkerId));
        if (checker == null) {
            throw new ResourceNotFoundException("Checker not found with ID: " + checkerId);
        }
        if (!"CHECKER".equalsIgnoreCase(checker.getRole())) {
            throw new InvalidApprovalException("User " + checkerId + " is not a CHECKER.");
        }
        if (!"ACTIVE".equalsIgnoreCase(checker.getStatus())) {
            throw new InvalidApprovalException("Checker account is not ACTIVE");
        }

        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        boolean requiresLevel2 = transaction.getAmount() != null
                && transaction.getAmount().compareTo(LEVEL2_THRESHOLD) >= 0;
        boolean isLevel2 = "LEVEL_2".equalsIgnoreCase(checker.getCheckerLevel());

        if (requiresLevel2 && !isLevel2 && approval.getStatus() == ApprovalStatus.PENDING) {
            throw new InvalidApprovalException(
                    "Level 1 access only. Cannot approve this transaction which requires Level 2 verification.");
        }

        if (!requiresLevel2 || isLevel2) {
            fundServiceClient.approveTransaction(transactionId, checkerId);
            approval.setStatus(ApprovalStatus.APPROVED);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} fully approved by checker {}", transactionId, checkerId);
        } else {
            approval.setStatus(ApprovalStatus.LEVEL1_APPROVED);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} approved by Level 1 checker {}", transactionId, checkerId);
        }

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse rejectTransactionByTxId(Long transactionId, String checkerId, String reason) {
        ApprovalRequest approval = approvalRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("No approval request found for transaction: " + transactionId));

        if (approval.getStatus() == ApprovalStatus.APPROVED
                || approval.getStatus() == ApprovalStatus.REJECTED) {
            throw new InvalidApprovalException(
                    "This transaction is already " + approval.getStatus());
        }

        CustomerUserDto checker = customerServiceClient.getUser(Long.valueOf(checkerId));
        if (checker == null) {
            throw new ResourceNotFoundException("Checker not found with ID: " + checkerId);
        }
        if (!"CHECKER".equalsIgnoreCase(checker.getRole())) {
            throw new InvalidApprovalException("User " + checkerId + " is not a CHECKER.");
        }
        if (!"ACTIVE".equalsIgnoreCase(checker.getStatus())) {
            throw new InvalidApprovalException("Checker account is not ACTIVE");
        }

        FundTransactionDto transaction = fundServiceClient.getTransaction(transactionId);
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }

        boolean requiresLevel2 = transaction.getAmount() != null
                && transaction.getAmount().compareTo(LEVEL2_THRESHOLD) >= 0;
        boolean isLevel2 = "LEVEL_2".equalsIgnoreCase(checker.getCheckerLevel());

        if (requiresLevel2 && !isLevel2) {
            throw new InvalidApprovalException(
                    "Level 1 access only. Cannot reject this transaction which requires Level 2 verification.");
        }

        fundServiceClient.rejectTransaction(transactionId, checkerId, reason);

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setComments(reason);
        approval = approvalRepository.save(approval);

        log.info("Transaction {} rejected by checker {}. Reason: {}", transactionId, checkerId, reason);
        return approvalMapper.toResponse(approval);
    }

    @Override
    public List<ApprovalResponse> getPendingApprovals() {
        return approvalRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(ApprovalStatus.PENDING, ApprovalStatus.LEVEL1_APPROVED))
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApprovalResponse> getApprovalsByChecker(String checkerId) {
        return approvalRepository.findByCheckerIdOrLevel2CheckerIdOrderByCreatedAtDesc(checkerId, checkerId)
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

    @Override
    public List<ApprovalResponse> getApprovalsByCheckerLevel(String level) {
        if ("LEVEL_1".equals(level)) {
            return approvalRepository.findByCheckerIdIsNotNullOrderByCreatedAtDesc()
                    .stream()
                    .map(approvalMapper::toResponse)
                    .toList();
        } else {
            return approvalRepository.findByLevel2CheckerIdIsNotNullOrderByCreatedAtDesc()
                    .stream()
                    .map(approvalMapper::toResponse)
                    .toList();
        }
    }

    private ApprovalRequest createApproval(Long transactionId, String makerId,
                                            String makerName, BigDecimal amount) {
        boolean requiresLevel2 = amount != null
                && amount.compareTo(LEVEL2_THRESHOLD) >= 0;

        ApprovalRequest approval = ApprovalRequest.builder()
                .transactionId(transactionId)
                .makerId(makerId)
                .makerName(makerName)
                .level2CheckerId(requiresLevel2 ? "REQUIRED" : null)
                .status(ApprovalStatus.PENDING)
                .build();

        approval = approvalRepository.save(approval);
        log.info("Approval auto-created for transaction {} by maker {}. Level2: {}",
                transactionId, makerId, requiresLevel2);
        return approval;
    }
}
