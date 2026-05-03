package com.deharri.ums.agency;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgencyRequestTableDropRunner implements ApplicationRunner {

    private final EntityManager entityManager;

    @Value("${agency.drop-request-table:false}")
    private boolean dropOnBoot;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!dropOnBoot) {
            return;
        }
        log.warn("AGENCY_DROP_REQUEST_TABLE enabled — dropping worker_agency_requests table");
        entityManager.createNativeQuery("DROP TABLE IF EXISTS worker_agency_requests CASCADE").executeUpdate();
        log.warn("worker_agency_requests dropped. Disable AGENCY_DROP_REQUEST_TABLE for subsequent boots.");
    }
}
