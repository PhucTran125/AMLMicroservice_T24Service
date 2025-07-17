package com.vpbankhackathon.t24_service.services;

import com.vpbankhackathon.t24_service.models.dtos.*;
import com.vpbankhackathon.t24_service.models.entities.AccountOpening;
import com.vpbankhackathon.t24_service.models.entities.Transaction;
import com.vpbankhackathon.t24_service.pubsub.producers.AccountOpeningProducer;
import com.vpbankhackathon.t24_service.pubsub.producers.TransactionProducer;
import com.vpbankhackathon.t24_service.pubsub.producers.VerifyCustomerProducer;
import com.vpbankhackathon.t24_service.repositories.AccountOpeningRepository;
import com.vpbankhackathon.t24_service.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
@EnableScheduling
public class DataSourceAndIntegrationService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountOpeningRepository accountOpeningRepository;

    @Autowired
    private AccountOpeningNotificationService notificationService;

    @Autowired
    private TransactionNotificationService transactionNotificationService;

    @Autowired
    private TransactionProducer transactionProducer;

    @Autowired
    private VerifyCustomerProducer verifyCustomerProducer;

    @Autowired
    AccountOpeningProducer accountOpeningProducer;

    private static final Random random = new Random();

    // API 1: Chuyển tiền
    public ResponseDTO processTransfer(TransactionRequestDTO request) {
        Transaction entity = mapToTransaction(request);
        transactionRepository.save(entity);

        // Send real-time notification for new transaction
        transactionNotificationService.notifyTransactionCreated(entity);

        // transactionProducer.sendMessage(entity);

        ResponseDTO response = new ResponseDTO();
        response.setTransactionId(entity.getId().toString());
        response.setStatus("accepted");
        if (request.getAmount() >= 400000000) {
            response.setMessage("Requires Customer screening");
            VerifyCustomerRequestDTO verifyCustomerRequest = mapToVerifyCustomerRequest(entity);
            verifyCustomerProducer.sendMessage(verifyCustomerRequest);
        } else {
            transactionProducer.sendMessage(entity);
        }
        return response;
    }

    // API 2: Mở tài khoản
    public ResponseDTO openAccount(AccountOpeningRequestDTO request) {
        AccountOpening entity = mapToAccountOpening(request);
        accountOpeningRepository.save(entity);

        // Send real-time notification for new account opening request
        notificationService.notifyAccountOpeningCreated(entity);

        VerifyCustomerRequestDTO verifyCustomerRequest = mapToVerifyCustomerRequest(entity);
        verifyCustomerProducer.sendMessage(verifyCustomerRequest);

        ResponseDTO response = new ResponseDTO();
        response.setTransactionId(UUID.randomUUID().toString());
        response.setStatus("OK");
        response.setMessage("Pending Customer screening");
        return response;
    }

    private Transaction mapToTransaction(TransactionRequestDTO dto) {
        Transaction entity = new Transaction();
        entity.setTimestamp(Instant.now().toEpochMilli());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setSourceAccountNumber(dto.getSourceAccountNumber());
        entity.setDestinationAccountNumber(dto.getDestinationAccountNumber());
        entity.setCustomerId(dto.getCustomerId());
        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerIdentificationNumber(dto.getCustomerIdentificationNumber());
        entity.setCountry(dto.getCountry());
        entity.setDate(LocalDateTime.parse(dto.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return entity;
    }

    private AccountOpening mapToAccountOpening(AccountOpeningRequestDTO dto) {
        AccountOpening entity = new AccountOpening();
        entity.setCustomerId(1000 + random.nextLong(9000)); // Generate a random 4-digit integer);
        entity.setTimestamp(Instant.now().toEpochMilli());
        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerIdentificationNumber(dto.getCustomerIdentificationNumber());
        entity.setDob(dto.getDob());
        entity.setNationality(dto.getNationality());
        entity.setResidentialAddress(dto.getResidentialAddress());
        return entity;
    }

    private VerifyCustomerRequestDTO mapToVerifyCustomerRequest(Transaction entity) {
        VerifyCustomerRequestDTO request = new VerifyCustomerRequestDTO();
        request.setCustomerId(entity.getCustomerId());
        request.setCustomerName(entity.getCustomerName());
        request.setCustomerIdentificationNumber(entity.getCustomerIdentificationNumber());
        request.setTaskType(T24AMLResult.TaskType.TRANSACTION_TRANSFER);
        return request;
    }

    private VerifyCustomerRequestDTO mapToVerifyCustomerRequest(AccountOpening entity) {
        VerifyCustomerRequestDTO request = new VerifyCustomerRequestDTO();
        request.setCustomerId(entity.getCustomerId());
        request.setCustomerName(entity.getCustomerName());
        request.setCustomerIdentificationNumber(entity.getCustomerIdentificationNumber());
        request.setDob(entity.getDob());
        request.setNationality(entity.getNationality());
        request.setResidentialAddress(entity.getResidentialAddress());
        request.setTaskType(T24AMLResult.TaskType.ACCOUNT_OPENING);
        return request;
    }

    public void processAMLResult(T24AMLResult result) {
        if (result.getId() == null)
            return;
        if (Objects.equals(result.getTaskType(), T24AMLResult.TaskType.ACCOUNT_OPENING)) {
            processAMLResultForAccountOpening(result);
        } else {
            processAMLResultForTransaction(result);
        }
    }

    private void processAMLResultForAccountOpening(T24AMLResult result) {
        AccountOpening accountOpening = accountOpeningRepository.findByCustomerIdAndStatus(
                result.getId(),
                AccountOpening.AccountOpeningStatus.PENDING);
        if (accountOpening == null)
            return;

        // Capture previous status for notification
        AccountOpening.AccountOpeningStatus previousStatus = accountOpening.getStatus();

        switch (result.getStatus()) {
            case CLEAR:
                accountOpening.setStatus(AccountOpening.AccountOpeningStatus.APPROVED);
                accountOpening.setResult("Account opening approved based on AML result");
                break;
            case SUSPENDED:
            case SUSPICIOUS:
                accountOpening.setStatus(AccountOpening.AccountOpeningStatus.REJECTED);
                accountOpening.setResult("Account opening rejected due to AML result: " + result.getReason());
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + result.getStatus());
        }

        accountOpeningRepository.save(accountOpening);

        // Send real-time notification for status change
        notificationService.notifyAccountOpeningStatusChanged(accountOpening, previousStatus);
    }

    private void processAMLResultForTransaction(T24AMLResult result) {
        Transaction transaction = transactionRepository.findByIdAndStatus(
                result.getId(),
                Transaction.TransactionStatus.PENDING);
        if (transaction == null)
            return;

        // Capture previous status for notification
        Transaction.TransactionStatus previousStatus = transaction.getStatus();

        if (Objects.equals(result.getResultType(), T24AMLResult.ResultType.CUSTOMER_SCREENING_RESULT)) {
            switch (result.getStatus()) {
                case CLEAR:
                    transaction.setStatus(Transaction.TransactionStatus.APPROVED);
                    transaction.setResult("Transaction approved after customer screening");
                    transactionRepository.save(transaction);
                    transactionProducer.sendMessage(transaction);
                    break;
                case SUSPENDED:
                case SUSPICIOUS:
                    transaction.setStatus(Transaction.TransactionStatus.REJECTED);
                    transaction.setResult("Transaction rejected due to customer screening: " + result.getReason());
                    transactionRepository.save(transaction);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown status: " + result.getStatus());
            }
        } else {
            switch (result.getStatus()) {
                case CLEAR:
                    transaction.setStatus(Transaction.TransactionStatus.APPROVED);
                    transaction.setResult("Transaction approved based on AML result");
                    break;
                case SUSPENDED:
                case SUSPICIOUS:
                    transaction.setStatus(Transaction.TransactionStatus.REJECTED);
                    transaction.setResult("Transaction rejected due to AML result: " + result.getReason());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown status: " + result.getStatus());
            }
            transactionRepository.save(transaction);
        }

        // Send real-time notification for status change
        transactionNotificationService.notifyTransactionStatusChanged(transaction, previousStatus);
    }
}