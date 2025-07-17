package com.vpbankhackathon.t24_service.services;

import com.vpbankhackathon.t24_service.models.dtos.AccountOpeningNotificationDTO;
import com.vpbankhackathon.t24_service.models.entities.AccountOpening;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountOpeningNotificationService {

  private static final Logger logger = LoggerFactory.getLogger(AccountOpeningNotificationService.class);
  private static final String TOPIC_ACCOUNT_OPENING = "/topic/account-opening";

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  /**
   * Sends notification when a new account opening request is created
   */
  public void notifyAccountOpeningCreated(AccountOpening accountOpening) {
    try {
      AccountOpeningNotificationDTO notification = new AccountOpeningNotificationDTO(
          AccountOpeningNotificationDTO.NotificationType.CREATED, accountOpening);

      messagingTemplate.convertAndSend(TOPIC_ACCOUNT_OPENING, notification);
      logger.info("Sent account opening created notification for customer ID: {}", accountOpening.getCustomerId());

    } catch (Exception e) {
      logger.error("Failed to send account opening created notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Sends notification when account opening status changes
   */
  public void notifyAccountOpeningStatusChanged(AccountOpening accountOpening,
      AccountOpening.AccountOpeningStatus previousStatus) {
    try {
      AccountOpeningNotificationDTO notification = new AccountOpeningNotificationDTO(accountOpening, previousStatus);

      messagingTemplate.convertAndSend(TOPIC_ACCOUNT_OPENING, notification);
      logger.info("Sent account opening status change notification for customer ID: {} from {} to {}",
          accountOpening.getCustomerId(), previousStatus, accountOpening.getStatus());

    } catch (Exception e) {
      logger.error("Failed to send account opening status change notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Sends notification when account opening is updated
   */
  public void notifyAccountOpeningUpdated(AccountOpening accountOpening) {
    try {
      AccountOpeningNotificationDTO notification = new AccountOpeningNotificationDTO(
          AccountOpeningNotificationDTO.NotificationType.UPDATED, accountOpening);

      messagingTemplate.convertAndSend(TOPIC_ACCOUNT_OPENING, notification);
      logger.info("Sent account opening updated notification for customer ID: {}", accountOpening.getCustomerId());

    } catch (Exception e) {
      logger.error("Failed to send account opening updated notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Sends a general notification message
   */
  public void sendCustomMessage(String message) {
    try {
      messagingTemplate.convertAndSend(TOPIC_ACCOUNT_OPENING + "/messages", message);
      logger.info("Sent custom message: {}", message);
    } catch (Exception e) {
      logger.error("Failed to send custom message: {}", e.getMessage(), e);
    }
  }
}