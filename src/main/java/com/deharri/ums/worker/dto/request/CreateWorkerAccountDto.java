package com.deharri.ums.worker.dto.request;

import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.worker.entity.Worker;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateWorkerAccountDto {

    @NotNull(message = "Worker type is required")
    private Worker.WorkerType workerType;

    @NotEmpty(message = "At least one skill is required")
    private List<@NotBlank(message = "Skill cannot be blank") String> skills;

    @NotBlank(message = "CNIC is required")
    @Pattern(regexp = "^[0-9]{5}-[0-9]{7}-[0-9]$", message = "CNIC must be in format: 12345-1234567-1")
    private String cnic;

    @Size(max = 5000, message = "Bio cannot exceed 5000 characters")
    private String bio;

    // Experience and Pricing
    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years cannot exceed 50")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Hourly rate must be a valid amount")
    private BigDecimal hourlyRate; // in PKR

    @DecimalMin(value = "0.0", inclusive = false, message = "Daily rate must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Daily rate must be a valid amount")
    private BigDecimal dailyRate; // in PKR (optional)

    // Location
    private PakistanCity city; // Primary city

    @Size(max = 200, message = "Area cannot exceed 200 characters")
    private String area; // Specific area/locality

    // Shop coordinates set by the LocationPickerField at signup. Required by
    // the mobile signup screen; left optional in the DTO so service-side
    // workflows that create workers without pinning a location (e.g. agency
    // adding a member on their behalf) still work.
    @DecimalMin(value = "-90.0",  message = "Shop latitude must be between -90 and 90")
    @DecimalMax(value = "90.0",   message = "Shop latitude must be between -90 and 90")
    private Double shopLatitude;

    @DecimalMin(value = "-180.0", message = "Shop longitude must be between -180 and 180")
    @DecimalMax(value = "180.0",  message = "Shop longitude must be between -180 and 180")
    private Double shopLongitude;

    private List<PakistanCity> serviceCities; // Cities where worker provides services

    // Languages
    private List<Language> languages; // Languages spoken

    // Agency (optional)
    private String agencyId; // UUID of agency if worker belongs to one

}
