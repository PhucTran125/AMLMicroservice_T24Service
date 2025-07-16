package com.vpbankhackathon.t24_service.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    private Long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // Generate a positive long ID
    private Long timestamp;
    private Long amount;
    private String currency;
    private Long sourceAccountNumber;
    private Long destinationAccountNumber;
    private String customerId;
    private String customerName;
    private String customerIdentificationNumber;
    private LocalDateTime date;
    private String country;
    private String status;
}
