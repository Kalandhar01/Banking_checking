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

        if (approvalRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new InvalidApprovalException(
                    "Transaction " + request.getTransactionId() + " is already submitted for approval");
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

        if (request.getLevel2CheckerId() != null && !request.getLevel2CheckerId().isBlank()) {
            CustomerUserDto level2Checker = customerServiceClient.getUser(Long.valueOf(request.getLevel2CheckerId()));
            if (level2Checker == null) {
                throw new ResourceNotFoundException("Level 2 checker not found with ID: " + request.getLevel2CheckerId());
            }
            if (!"CHECKER".equalsIgnoreCase(level2Checker.getRole())) {
                throw new InvalidApprovalException(
                        "User " + request.getLevel2CheckerId() + " is not a CHECKER.");
            }
            if (!"ACTIVE".equalsIgnoreCase(level2Checker.getStatus())) {
                throw new InvalidApprovalException("Level 2 checker account is not ACTIVE");
            }
        }

        ApprovalRequest approval = ApprovalRequest.builder()
                .transactionId(request.getTransactionId())
                .makerId(request.getMakerId())
                .makerName(maker.getEmployeeName())
                .checkerId(request.getCheckerId())
                .level2CheckerId(request.getLevel2CheckerId())
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

        if (approval.getStatus() == ApprovalStatus.APPROVED
                || approval.getStatus() == ApprovalStatus.REJECTED) {
            throw new InvalidApprovalException(
                    "Approval is already " + approval.getStatus());
        }

        boolean isLevel1 = approval.getCheckerId().equals(checkerId);
        boolean isLevel2 = approval.getLevel2CheckerId() != null && approval.getLevel2CheckerId().equals(checkerId);

        if (!isLevel1 && !isLevel2) {
            throw new InvalidApprovalException(
                    "This approval request is not assigned to checker " + checkerId);
        }

        if (isLevel1 && approval.getStatus() != ApprovalStatus.PENDING) {
            throw new InvalidApprovalException(
                    "Level 1 approval is not in PENDING state");
        }

        if (isLevel2 && approval.getStatus() != ApprovalStatus.LEVEL1_APPROVED) {
            throw new InvalidApprovalException(
                    "Level 2 approval requires Level 1 to approve first");
        }

        FundTransactionDto transaction = fundServiceClient.getTransaction(approval.getTransactionId());
        if (transaction == null) {
            throw new ResourceNotFoundException("Transaction not found: " + approval.getTransactionId());
        }

        if (isLevel1 && approval.getLevel2CheckerId() != null) {
            approval.setStatus(ApprovalStatus.LEVEL1_APPROVED);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} approved by Level 1 checker {}", approval.getTransactionId(), checkerId);
        } else {
            fundServiceClient.approveTransaction(approval.getTransactionId(), checkerId);
            approval.setStatus(ApprovalStatus.APPROVED);
            approval = approvalRepository.save(approval);
            log.info("Transaction {} fully approved by checker {}", approval.getTransactionId(), checkerId);
        }

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse rejectTransaction(Long approvalId, String checkerId, String reason) {
        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found with ID: " + approvalId));

        if (approval.getStatus() == ApprovalStatus.APPROVED
                || approval.getStatus() == ApprovalStatus.REJECTED) {
            throw new InvalidApprovalException(
                    "Approval is already " + approval.getStatus());
        }

        boolean isLevel1 = approval.getCheckerId().equals(checkerId);
        boolean isLevel2 = approval.getLevel2CheckerId() != null && approval.getLevel2CheckerId().equals(checkerId);

        if (!isLevel1 && !isLevel2) {
            throw new InvalidApprovalException(
                    "This approval request is not assigned to checker " + checkerId);
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
}
