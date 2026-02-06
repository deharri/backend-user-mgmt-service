package com.deharri.ums.agency.dto.response;

import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.enums.RequestStatus;
import com.deharri.ums.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerAgencyRequestDto {

    private String requestId;

    // Worker info
    private String workerId;
    private String workerName;
    private String workerType;

    // Agency info
    private String agencyId;
    private String agencyName;

    private RequestType requestType;
    private RequestStatus status;
    private AgencyRole proposedRole;

    private String message;
    private String responseMessage;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

}
