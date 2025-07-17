package com.vpbankhackathon.t24_service.models.dtos;

import com.vpbankhackathon.t24_service.models.entities.AccountOpening;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountOpeningNotificationDTO {

  public enum NotificationType {
    CREATED, UPDATED, STATUS_CHANGED
  }

  private NotificationType type;
  private Long id;
  private Long customerId;
  private String customerName;
  private String customerIdentificationNumber;
  private String dob;
  private String nationality;
  private String residentialAddress;
  private AccountOpening.AccountOpeningStatus status;
  private AccountOpening.AccountOpeningStatus previousStatus;
  private String result = "N/A"; // Default to "n/a" if not set
  private Long timestamp;
  private LocalDateTime eventTime;

  // Constructor for creating notification from AccountOpening entity
  public AccountOpeningNotificationDTO(NotificationType type, AccountOpening accountOpening) {
    this.type = type;
    this.id = accountOpening.getId();
    this.customerId = accountOpening.getCustomerId();
    this.customerName = accountOpening.getCustomerName();
    this.customerIdentificationNumber = accountOpening.getCustomerIdentificationNumber();
    this.dob = accountOpening.getDob();
    this.nationality = accountOpening.getNationality();
    this.residentialAddress = accountOpening.getResidentialAddress();
    this.status = accountOpening.getStatus();
    this.result = "N/A"; // Default to "n/a" if not set
    this.timestamp = accountOpening.getTimestamp();
    this.eventTime = LocalDateTime.now();
  }

  // Constructor for status change notifications
  public AccountOpeningNotificationDTO(AccountOpening accountOpening,
      AccountOpening.AccountOpeningStatus previousStatus) {
    this(NotificationType.STATUS_CHANGED, accountOpening);
    this.previousStatus = previousStatus;
    this.result = accountOpening.getResult();
  }
}