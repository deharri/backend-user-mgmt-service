package com.deharri.ums.worker.dto.request;

import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
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
public class UpdateWorkerProfileDto {

    private List<@NotBlank(message = "Skill cannot be blank") String> skills;

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

    private List<PakistanCity> serviceCities; // Cities where worker provides services

    // Languages
    private List<Language> languages; // Languages spoken

}
