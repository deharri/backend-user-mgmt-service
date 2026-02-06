package com.deharri.ums.agency.controller;

import com.deharri.ums.agency.AgencyService;
import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.agency.dto.request.UpdateAgencyDto;
import com.deharri.ums.agency.dto.response.AgencyListItemDto;
import com.deharri.ums.agency.dto.response.AgencyProfileResponseDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessageDto> createAgency(
            @Valid @RequestBody CreateAgencyDto createAgencyDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.createAgency(createAgencyDto));
    }

    @GetMapping
    public ResponseEntity<List<AgencyListItemDto>> getAllAgencies() {
        return ResponseEntity.ok(agencyService.getAllAgencies());
    }

    @GetMapping("/{agencyId}")
    public ResponseEntity<AgencyProfileResponseDto> getAgencyById(@PathVariable String agencyId) {
        return ResponseEntity.ok(agencyService.getAgencyById(agencyId));
    }

    @GetMapping("/me")
    public ResponseEntity<AgencyProfileResponseDto> getMyAgencyProfile() {
        return ResponseEntity.ok(agencyService.getMyAgencyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ResponseMessageDto> updateMyAgencyProfile(
            @Valid @RequestBody UpdateAgencyDto updateDto
    ) {
        return ResponseEntity.ok(agencyService.updateMyAgencyProfile(updateDto));
    }

    @PostMapping("/license/upload")
    public ResponseEntity<ResponseMessageDto> uploadLicenseDocument(
            @RequestPart MultipartFile licenseDocument
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.uploadLicenseDocument(licenseDocument));
    }

}
