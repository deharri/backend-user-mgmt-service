package com.deharri.ums.worker;

import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    Optional<Worker> findByCoreUser(CoreUser currentUser);

    Optional<Worker> findByCoreUser_UserId(UUID userId);

    List<Worker> findAllBySubscriptionActiveTrue();

    @Query(value = """
        SELECT * FROM worker w
        WHERE w.subscription_active = true
        AND w.shop_latitude IS NOT NULL
        AND w.shop_longitude IS NOT NULL
        AND (:workerType IS NULL OR w.worker_type = :workerType)
        AND (6371 * acos(LEAST(1.0,
            cos(radians(:lat)) * cos(radians(w.shop_latitude)) *
            cos(radians(w.shop_longitude) - radians(:lng)) +
            sin(radians(:lat)) * sin(radians(w.shop_latitude))
        ))) <= :radiusKm
        """, nativeQuery = true)
    List<Worker> findNearbySubscribedWorkers(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radiusKm") double radiusKm,
        @Param("workerType") String workerType
    );
}
