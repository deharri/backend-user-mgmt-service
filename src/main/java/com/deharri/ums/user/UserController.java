package com.deharri.ums.user;

import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

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

}
