package com.vpbankhackathon.t24_service.repositories;

import com.vpbankhackathon.t24_service.models.entities.AccountOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountOpeningRepository extends JpaRepository<AccountOpening, Long> {
    AccountOpening findByCustomerIdAndStatus(Long customerId, AccountOpening.AccountOpeningStatus status);

    List<AccountOpening> findByStatus(AccountOpening.AccountOpeningStatus status);
}
