package com.vpbankhackathon.t24_service.controllers;

import com.vpbankhackathon.t24_service.models.dtos.AccountOpeningRequestDTO;
import com.vpbankhackathon.t24_service.models.dtos.ResponseDTO;
import com.vpbankhackathon.t24_service.models.dtos.TransactionRequestDTO;
import com.vpbankhackathon.t24_service.models.entities.AccountOpening;
import com.vpbankhackathon.t24_service.models.entities.Transaction;
import com.vpbankhackathon.t24_service.repositories.AccountOpeningRepository;
import com.vpbankhackathon.t24_service.repositories.TransactionRepository;
import com.vpbankhackathon.t24_service.services.DataSourceAndIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/t24")
public class T24Controller {

    @Autowired
    private DataSourceAndIntegrationService service;

    @Autowired
    private AccountOpeningRepository accountOpeningRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/transactions/transfer")
    public ResponseEntity<ResponseDTO> transfer(@RequestBody TransactionRequestDTO transactionRequestDTO) {
        return ResponseEntity.ok(service.processTransfer(transactionRequestDTO));
    }

    @PostMapping("/accounts/open")
    public ResponseEntity<ResponseDTO> openAccount(@RequestBody AccountOpeningRequestDTO accountOpeningRequestDTO) {
        return ResponseEntity.ok(service.openAccount(accountOpeningRequestDTO));
    }

    /**
     * Get all account opening records for real-time display
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountOpening>> getAllAccountOpenings() {
        List<AccountOpening> accountOpenings = accountOpeningRepository.findAll();
        return ResponseEntity.ok(accountOpenings);
    }

    /**
     * Get account opening records by status
     */
    @GetMapping("/accounts/status/{status}")
    public ResponseEntity<List<AccountOpening>> getAccountOpeningsByStatus(@PathVariable String status) {
        try {
            AccountOpening.AccountOpeningStatus accountStatus = AccountOpening.AccountOpeningStatus
                    .valueOf(status.toUpperCase());
            List<AccountOpening> accountOpenings = accountOpeningRepository.findByStatus(accountStatus);
            return ResponseEntity.ok(accountOpenings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get specific account opening by ID
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountOpening> getAccountOpeningById(@PathVariable Long id) {
        return accountOpeningRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get recent account openings (for demo purposes)
     */
    @GetMapping("/accounts/recent")
    public ResponseEntity<List<AccountOpening>> getRecentAccountOpenings(@RequestParam(defaultValue = "10") int limit) {
        List<AccountOpening> accountOpenings = accountOpeningRepository.findAll();
        // Return latest records (simplified - in real scenario you'd use proper
        // ordering)
        int size = accountOpenings.size();
        int fromIndex = Math.max(0, size - limit);
        return ResponseEntity.ok(accountOpenings.subList(fromIndex, size));
    }

    // ============= TRANSACTION ENDPOINTS =============

    /**
     * Get all transactions for real-time display
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by status
     */
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        try {
            Transaction.TransactionStatus transactionStatus = Transaction.TransactionStatus
                    .valueOf(status.toUpperCase());
            List<Transaction> transactions = transactionRepository.findByStatus(transactionStatus);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get specific transaction by ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get transactions by customer ID
     */
    @GetMapping("/transactions/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomerId(@PathVariable Long customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get recent transactions (for demo purposes)
     */
    @GetMapping("/transactions/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions(@RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionRepository.findAll();
        // Return latest records (simplified - in real scenario you'd use proper
        // ordering)
        int size = transactions.size();
        int fromIndex = Math.max(0, size - limit);
        return ResponseEntity.ok(transactions.subList(fromIndex, size));
    }
}
