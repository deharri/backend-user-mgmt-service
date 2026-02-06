package com.deharri.ums.agency.dto.request;

import com.deharri.ums.enums.AgencyRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendInvitationDto {

    @NotBlank(message = "Worker ID is required")
    private String workerId; // UUID of the worker

    @NotNull(message = "Agency role is required")
    private AgencyRole proposedRole; // Role being offered

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message; // Optional message

}
