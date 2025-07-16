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
    private TransactionProducer transactionProducer;

    @Autowired
    private VerifyCustomerProducer verifyCustomerProducer;

    @Autowired
    AccountOpeningProducer accountOpeningProducer;

    private static final Random random = new Random();

    // API 1: Chuyển tiền
    public ResponseDTO processTransfer(TransactionRequestDTO request) {
        Transaction entity = mapToTransaction(request);

        entity.setStatus("pending");
        transactionRepository.save(entity);
//        transactionProducer.sendMessage(entity);

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

        entity.setStatus("sent");
        accountOpeningRepository.save(entity);
        accountOpeningProducer.sendMessage(entity);

        ResponseDTO response = new ResponseDTO();
        response.setTransactionId(UUID.randomUUID().toString());
        response.setStatus("accepted");
        response.setMessage("Pending KYC screening");
        return response;
    }

    // Mô phỏng T24 tạo sự kiện
//    @Scheduled(fixedRate = 5000)
//    public void simulateT24Events() {
//        if (random.nextBoolean()) {
//            TransactionRequestDTO transfer = generateTransferRequest();
//            processTransfer(transfer);
//        } else {
//            AccountOpeningRequestDTO account = generateAccountOpeningRequest();
//            openAccount(account);
//        }
//    }

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
        request.setTransactionId(entity.getId().toString());
        request.setCustomerId(entity.getCustomerId());
        request.setCustomerName(entity.getCustomerName());
        request.setCustomerIdentificationNumber(entity.getCustomerIdentificationNumber());
        return request;
    }

    public void processAMLResult(T24AMLResult result) {
        if (result.getType() == "CUSTOMER_SCREENING_RESULT") {
            AccountOpening accountOpening = accountOpeningRepository.getReferenceById(result.getId());

            accountOpening.setStatus(result.getStatus());
            accountOpeningRepository.save(accountOpening);
        } else {
            Transaction transaction = transactionRepository.getReferenceById(result.getId());

            transaction.setStatus(result.getStatus());
            transactionRepository.save(transaction);
        }

    }
}