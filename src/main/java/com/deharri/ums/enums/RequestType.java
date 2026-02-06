package com.deharri.ums.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RequestType {
    WORKER_REQUEST("Worker Request to Join Agency", "Worker requesting to join an agency"),
    AGENCY_INVITATION("Agency Invitation to Worker", "Agency inviting a worker to join");

    private final String displayName;
    private final String description;
}
