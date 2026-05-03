package com.deharri.ums.user.controller.internal;

import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.dto.response.InternalUserDto;
import com.deharri.ums.user.entity.CoreUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal User API", description = "Internal endpoints for inter-service user lookups")
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user's basic identity (for chat-service display name resolution)")
    @Transactional(readOnly = true)
    public ResponseEntity<InternalUserDto> getUserById(@PathVariable UUID userId) {
        CoreUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return ResponseEntity.ok(InternalUserDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build());
    }
}
