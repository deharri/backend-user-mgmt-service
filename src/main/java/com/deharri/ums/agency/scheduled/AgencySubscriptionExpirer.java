package com.deharri.ums.agency.scheduled;

import com.deharri.ums.agency.AgencyRepository;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.enums.AgencySubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgencySubscriptionExpirer {

    private final AgencyRepository agencyRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireStaleSubscriptions() {
        List<Agency> stale = agencyRepository
                .findAllBySubscriptionStatusAndSubscriptionExpiresAtBefore(
                        AgencySubscriptionStatus.ACTIVE, LocalDateTime.now());
        for (Agency a : stale) {
            a.setSubscriptionStatus(AgencySubscriptionStatus.EXPIRED);
        }
        if (!stale.isEmpty()) {
            agencyRepository.saveAll(stale);
            log.info("Expired {} agency subscriptions", stale.size());
        }
    }
}
