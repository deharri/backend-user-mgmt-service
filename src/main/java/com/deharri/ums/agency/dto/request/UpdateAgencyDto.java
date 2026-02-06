package com.deharri.ums.agency.dto.request;

import com.deharri.ums.enums.PakistanCity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateAgencyDto {

    @Size(min = 3, max = 200, message = "Agency name must be between 3 and 200 characters")
    private String agencyName;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Contact number must be a valid phone number")
    private String contactNumber;

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String contactEmail;

    private PakistanCity city;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    private List<PakistanCity> serviceCities;

    @Size(max = 100, message = "License number cannot exceed 100 characters")
    private String licenseNumber;

}
