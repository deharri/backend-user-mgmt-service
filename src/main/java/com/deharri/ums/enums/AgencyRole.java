package com.deharri.ums.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AgencyRole {
    AGENCY_ADMIN("Agency Admin", "Full access to all agency operations"),
    AGENCY_MANAGER("Agency Manager", "Can add and edit workers but cannot delete"),
    AGENCY_WORKER("Agency Worker", "Read-only access in agency context");

    private final String displayName;
    private final String description;
}
