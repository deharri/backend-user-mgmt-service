package com.deharri.ums.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirror of jobs-service's {@code JobLifecycleEvent}. JSON shape is the contract.
 * UMS only consumes {@code job.confirmed}; CANCELLED variant is not subscribed to here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobLifecycleEvent {

    public enum Type { CONFIRMED, CANCELLED }

    private Type type;
    private UUID jobId;
    private UUID consumerId;
    private UUID assignedWorkerId;
    private UUID assignedAgencyId;
    private UUID dispatchedWorkerId;
    private Instant occurredAt;
}
