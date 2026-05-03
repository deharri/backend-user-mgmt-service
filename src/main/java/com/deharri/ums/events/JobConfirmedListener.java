package com.deharri.ums.events;

import com.deharri.ums.agency.AgencyRepository;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.worker.WorkerRepository;
import com.deharri.ums.worker.entity.Worker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Increments {@code totalJobsCompleted} on workers and/or agencies in response to
 * {@code job.confirmed} events. Replaces the previous synchronous PUT-driven sync.
 *
 * <p>Increment-by-1 semantics. Re-delivery of an event would over-count in theory,
 * but Spring Kafka's default at-least-once delivery with auto-commit makes that rare
 * in practice. For FYP scope this is a worthwhile trade for the simplicity gain.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobConfirmedListener {

    private final WorkerRepository workerRepository;
    private final AgencyRepository agencyRepository;

    @KafkaListener(topics = "job.confirmed", groupId = "user-mgmt-service")
    @Transactional
    public void onJobConfirmed(JobLifecycleEvent event) {
        if (event == null || event.getJobId() == null) {
            log.warn("Ignoring malformed job.confirmed event: {}", event);
            return;
        }
        log.info("UMS received job.confirmed for job {}", event.getJobId());

        UUID workerUserId = event.getDispatchedWorkerId() != null
                ? event.getDispatchedWorkerId()
                : event.getAssignedWorkerId();
        if (workerUserId != null) {
            incrementWorkerJobsCompleted(workerUserId);
        }
        if (event.getAssignedAgencyId() != null) {
            incrementAgencyJobsCompleted(event.getAssignedAgencyId());
        }
    }

    private void incrementWorkerJobsCompleted(UUID workerUserId) {
        try {
            Worker worker = workerRepository.findByCoreUser_UserId(workerUserId)
                    .or(() -> workerRepository.findById(workerUserId))
                    .orElse(null);
            if (worker == null) {
                log.warn("Worker not found for userId {} — skipping stat increment", workerUserId);
                return;
            }
            int current = worker.getTotalJobsCompleted() == null ? 0 : worker.getTotalJobsCompleted();
            worker.setTotalJobsCompleted(current + 1);
            workerRepository.save(worker);
            log.info("Incremented worker {} totalJobsCompleted to {}", workerUserId, current + 1);
        } catch (Exception e) {
            log.error("Failed to increment worker stats for {}: {}", workerUserId, e.getMessage());
        }
    }

    private void incrementAgencyJobsCompleted(UUID agencyId) {
        try {
            Agency agency = agencyRepository.findById(agencyId).orElse(null);
            if (agency == null) {
                log.warn("Agency not found for id {} — skipping stat increment", agencyId);
                return;
            }
            int current = agency.getTotalJobsCompleted() == null ? 0 : agency.getTotalJobsCompleted();
            agency.setTotalJobsCompleted(current + 1);
            agencyRepository.save(agency);
            log.info("Incremented agency {} totalJobsCompleted to {}", agencyId, current + 1);
        } catch (Exception e) {
            log.error("Failed to increment agency stats for {}: {}", agencyId, e.getMessage());
        }
    }
}
