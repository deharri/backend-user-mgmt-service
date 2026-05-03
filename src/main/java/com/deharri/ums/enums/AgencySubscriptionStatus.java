package com.deharri.ums.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AgencySubscriptionStatus {
    INACTIVE("Inactive", "Agency created but payment not yet received"),
    ACTIVE("Active", "Subscription paid and within validity window"),
    EXPIRED("Expired", "Subscription period has lapsed"),
    CANCELLED("Cancelled", "Agency owner cancelled the subscription");

    private final String displayName;
    private final String description;
}
