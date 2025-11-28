package com.deharri.ums.user;

import com.deharri.ums.amazon.dto.SignedUrlDto;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserProfileDto>> getAllUserProfiles() {
        return ResponseEntity.ok(userService.getAllUserProfiles());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable UUID uuid) {
        return ResponseEntity.ok(userService.getUserProfile(uuid));
    }

    @PutMapping("/password")
    public ResponseEntity<ResponseMessageDto> updatePassword(@RequestBody UserPasswordUpdateDto dto) {
        return ResponseEntity.ok(userService.updatePassword(dto));
    }

    @PutMapping("/email")
    public ResponseEntity<ResponseMessageDto> updateEmail(@RequestBody UserEmailUpdateDto dto) {
        return ResponseEntity.ok(userService.updateEmail(dto));
    }

    @PutMapping("/phone")
    public ResponseEntity<ResponseMessageDto> updatePhoneNo(@RequestBody UserPhoneNoUpdateDto dto) {
        return ResponseEntity.ok(userService.updatePhoneNo(dto));
    }

    @PostMapping("/picture")
    public ResponseEntity<ResponseMessageDto> updateProfilePicture(@NotNull @RequestPart MultipartFile picture) {
        return ResponseEntity.status(201).body(userService.updateProfilePicture(picture));
    }

    @GetMapping("/picture")
    public ResponseEntity<SignedUrlDto> getMyProfilePictureUrl() {
        return ResponseEntity.ok(userService.getMyProfilePictureUrl());
    }

}
