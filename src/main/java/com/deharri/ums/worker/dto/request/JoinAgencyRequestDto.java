package com.deharri.ums.worker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinAgencyRequestDto {

    @NotBlank(message = "Agency ID is required")
    private String agencyId; // UUID of the agency

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message; // Optional message from worker

}
