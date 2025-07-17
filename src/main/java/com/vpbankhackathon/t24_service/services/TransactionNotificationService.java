package com.vpbankhackathon.t24_service.services;

import com.vpbankhackathon.t24_service.models.dtos.TransactionNotificationDTO;
import com.vpbankhackathon.t24_service.models.entities.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionNotificationService {

  private static final Logger logger = LoggerFactory.getLogger(TransactionNotificationService.class);
  private static final String TOPIC_TRANSACTION = "/topic/transaction";

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  /**
   * Sends notification when a new transaction is created
   */
  public void notifyTransactionCreated(Transaction transaction) {
    try {
      TransactionNotificationDTO notification = new TransactionNotificationDTO(
          TransactionNotificationDTO.NotificationType.CREATED, transaction);

      messagingTemplate.convertAndSend(TOPIC_TRANSACTION, notification);
      logger.info("Sent transaction created notification for transaction ID: {}", transaction.getId());

    } catch (Exception e) {
      logger.error("Failed to send transaction created notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Sends notification when transaction status changes
   */
  public void notifyTransactionStatusChanged(Transaction transaction,
      Transaction.TransactionStatus previousStatus) {
    try {
      TransactionNotificationDTO notification = new TransactionNotificationDTO(transaction, previousStatus);

      messagingTemplate.convertAndSend(TOPIC_TRANSACTION, notification);
      logger.info("Sent transaction status change notification for transaction ID: {} from {} to {}",
          transaction.getId(), previousStatus, transaction.getStatus());

    } catch (Exception e) {
      logger.error("Failed to send transaction status change notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Sends notification when transaction is updated
   */
  public void notifyTransactionUpdated(Transaction transaction) {
    try {
      TransactionNotificationDTO notification = new TransactionNotificationDTO(
          TransactionNotificationDTO.NotificationType.UPDATED, transaction);

      messagingTemplate.convertAndSend(TOPIC_TRANSACTION, notification);
      logger.info("Sent transaction updated notification for transaction ID: {}", transaction.getId());

    } catch (Exception e) {
      logger.error("Failed to send transaction updated notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Sends a general notification message
   */
  public void sendCustomMessage(String message) {
    try {
      messagingTemplate.convertAndSend(TOPIC_TRANSACTION + "/messages", message);
      logger.info("Sent custom message: {}", message);
    } catch (Exception e) {
      logger.error("Failed to send custom message: {}", e.getMessage(), e);
    }
  }
}