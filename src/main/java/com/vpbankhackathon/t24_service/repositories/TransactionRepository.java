package com.vpbankhackathon.t24_service.repositories;

import com.vpbankhackathon.t24_service.models.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findByIdAndStatus(Long id, Transaction.TransactionStatus status);

    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    /**
     * Find transactions by customer ID
     */
    List<Transaction> findByCustomerId(Long customerId);

    /**
     * Find transactions by customer ID and status
     */
    List<Transaction> findByCustomerIdAndStatus(Long customerId, Transaction.TransactionStatus status);

    /**
     * Updates the status of a transaction by ID
     * 
     * @param id     the transaction ID
     * @param status the new status to set
     * @return number of rows affected (1 if successful, 0 if transaction not found)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.status = :status WHERE t.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") Transaction.TransactionStatus status);

    /**
     * Updates the status of a transaction by ID, but only if current status matches
     * expected status
     * 
     * @param id            the transaction ID
     * @param newStatus     the new status to set
     * @param currentStatus the expected current status
     * @return number of rows affected (1 if successful, 0 if transaction not found
     *         or status doesn't match)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.status = :newStatus WHERE t.id = :id AND t.status = :currentStatus")
    int updateStatusByIdAndCurrentStatus(@Param("id") Long id,
            @Param("newStatus") Transaction.TransactionStatus newStatus,
            @Param("currentStatus") Transaction.TransactionStatus currentStatus);
}
