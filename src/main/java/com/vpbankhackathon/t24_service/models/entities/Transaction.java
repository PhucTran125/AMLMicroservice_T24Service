package com.vpbankhackathon.t24_service.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_transfers")
@Data
public class Transaction {
    public enum TransactionStatus {
        PENDING("PENDING"),
        APPROVED("APPROVED"),
        REJECTED("REJECTED");

        private final String status;

        TransactionStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    @Id
    private Long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // Generate a positive long ID
    private Long timestamp;
    private Long amount;
    private String currency;
    @Column(name = "source_account_number")
    private Long sourceAccountNumber;
    @Column(name = "destination_account_number")
    private Long destinationAccountNumber;
    @Column(name = "customer_id")
    private Long customerId;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_identification_number")
    private String customerIdentificationNumber;
    private LocalDateTime date;
    private String country;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;
    private String result = "N/A"; // Default result message
}
