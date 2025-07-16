package com.vpbankhackathon.t24_service.repositories;

import com.vpbankhackathon.t24_service.models.entities.AccountOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;


@Component
public interface AccountOpeningRepository extends JpaRepository<AccountOpening, Long> {
}
