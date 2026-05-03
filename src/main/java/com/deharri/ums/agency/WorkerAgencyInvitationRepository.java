package com.deharri.ums.agency;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.WorkerAgencyInvitation;
import com.deharri.ums.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerAgencyInvitationRepository extends JpaRepository<WorkerAgencyInvitation, UUID> {

    Optional<WorkerAgencyInvitation> findByWorkerAndAgencyAndStatus(
            Worker worker, Agency agency, WorkerAgencyInvitation.Status status);

    List<WorkerAgencyInvitation> findByWorkerAndStatusOrderByCreatedAtDesc(
            Worker worker, WorkerAgencyInvitation.Status status);

    /** All invitations sent by an agency, newest first. Used by the dashboard. */
    List<WorkerAgencyInvitation> findByAgencyOrderByCreatedAtDesc(Agency agency);
}
