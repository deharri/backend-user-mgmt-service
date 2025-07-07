package com.deharri.ums.auth;

import com.deharri.ums.auth.dto.LoginRequestDto;
import com.deharri.ums.auth.dto.RefreshTokenDto;
import com.deharri.ums.auth.dto.RegisterRequestDto;
import com.deharri.ums.auth.dto.AuthResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @RequestBody RegisterRequestDto registerRequestDto
    ) {
        return ResponseEntity.status(CREATED).body(authService.register(registerRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto
    ) throws BadCredentialsException {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @RequestBody RefreshTokenDto refreshTokenDto
    ) {
        return ResponseEntity.ok(authService.refresh(refreshTokenDto));
    }



}
