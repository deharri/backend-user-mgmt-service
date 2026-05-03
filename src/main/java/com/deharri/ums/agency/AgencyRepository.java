package com.deharri.ums.agency;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.enums.AgencySubscriptionStatus;
import com.deharri.ums.user.entity.CoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, UUID> {

    Optional<Agency> findByCoreUser(CoreUser coreUser);

    boolean existsByAgencyName(String agencyName);

    List<Agency> findAllBySubscriptionStatusAndSubscriptionExpiresAtBefore(
            AgencySubscriptionStatus status, LocalDateTime cutoff);

}
