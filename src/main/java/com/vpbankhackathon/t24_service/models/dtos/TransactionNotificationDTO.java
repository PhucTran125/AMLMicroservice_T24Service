package com.vpbankhackathon.t24_service.models.dtos;

import com.vpbankhackathon.t24_service.models.entities.Transaction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionNotificationDTO {

  public enum NotificationType {
    CREATED, UPDATED, STATUS_CHANGED
  }

  private NotificationType type;
  private Long id;
  private Long amount;
  private String currency;
  private Long sourceAccountNumber;
  private Long destinationAccountNumber;
  private Long customerId;
  private String customerName;
  private String customerIdentificationNumber;
  private String country;
  private Transaction.TransactionStatus status;
  private Transaction.TransactionStatus previousStatus;
  private String result = "N/A"; // Default to "N/A" if not set
  private Long timestamp;
  private LocalDateTime eventTime;

  // Constructor for creating notification from Transaction entity
  public TransactionNotificationDTO(NotificationType type, Transaction transaction) {
    this.type = type;
    this.id = transaction.getId();
    this.amount = transaction.getAmount();
    this.currency = transaction.getCurrency();
    this.sourceAccountNumber = transaction.getSourceAccountNumber();
    this.destinationAccountNumber = transaction.getDestinationAccountNumber();
    this.customerId = transaction.getCustomerId();
    this.customerName = transaction.getCustomerName();
    this.customerIdentificationNumber = transaction.getCustomerIdentificationNumber();
    this.country = transaction.getCountry();
    this.status = transaction.getStatus();
    this.result = transaction.getResult() != null ? transaction.getResult() : "N/A";
    this.timestamp = transaction.getTimestamp();
    this.eventTime = LocalDateTime.now();
  }

  // Constructor for status change notifications
  public TransactionNotificationDTO(Transaction transaction,
      Transaction.TransactionStatus previousStatus) {
    this(NotificationType.STATUS_CHANGED, transaction);
    this.previousStatus = previousStatus;
  }
}