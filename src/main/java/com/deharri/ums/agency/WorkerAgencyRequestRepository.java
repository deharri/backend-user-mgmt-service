package com.deharri.ums.agency;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.WorkerAgencyRequest;
import com.deharri.ums.enums.RequestStatus;
import com.deharri.ums.enums.RequestType;
import com.deharri.ums.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerAgencyRequestRepository extends JpaRepository<WorkerAgencyRequest, UUID> {

    List<WorkerAgencyRequest> findByAgency(Agency agency);

    List<WorkerAgencyRequest> findByWorker(Worker worker);

    List<WorkerAgencyRequest> findByAgencyAndStatus(Agency agency, RequestStatus status);

    List<WorkerAgencyRequest> findByWorkerAndStatus(Worker worker, RequestStatus status);

    Optional<WorkerAgencyRequest> findByWorkerAndAgencyAndStatus(Worker worker, Agency agency, RequestStatus status);

    boolean existsByWorkerAndAgencyAndStatusAndRequestType(Worker worker, Agency agency, RequestStatus status, RequestType requestType);

}
