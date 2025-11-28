package com.deharri.ums.worker;

import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, UUID> {
    Optional<Worker> findByCoreUser(CoreUser currentUser);
}
