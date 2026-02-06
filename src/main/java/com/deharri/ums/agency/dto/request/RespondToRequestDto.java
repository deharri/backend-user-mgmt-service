package com.deharri.ums.agency.dto.request;

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
public class RespondToRequestDto {

    @NotNull(message = "Accept decision is required")
    private Boolean accept; // true = accept, false = reject

    @Size(max = 500, message = "Response message cannot exceed 500 characters")
    private String responseMessage; // Optional response message

}
