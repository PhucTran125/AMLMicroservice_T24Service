package com.vpbankhackathon.t24_service.models.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_openings")
@Data
public class AccountOpening {
    public enum AccountOpeningStatus {
        PENDING, APPROVED, REJECTED
    }

    @Id
    private Long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; // Generate a positive long ID
    private Long timestamp = Instant.now().toEpochMilli();
    @Column(name = "customer_id")
    private Long customerId; // Generate a positive long ID
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_identification_number")
    private String customerIdentificationNumber;
    private String dob;
    private String nationality;
    @Column(name = "residential_address")
    private String residentialAddress;
    @Enumerated(EnumType.STRING)
    private AccountOpeningStatus status = AccountOpeningStatus.PENDING;
    @Column(name = "result")
    private String result;
}
