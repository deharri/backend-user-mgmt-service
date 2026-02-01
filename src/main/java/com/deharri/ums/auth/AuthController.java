package com.deharri.ums.auth;

import com.deharri.ums.auth.dto.request.LoginRequestDto;
import com.deharri.ums.auth.dto.request.RefreshTokenDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.auth.dto.response.AuthResponseDto;
import com.deharri.ums.error.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

/**
 * REST Controller for authentication operations.
 * <p>
 * Provides endpoints for user registration, login, token refresh, and logout.
 * All endpoints are publicly accessible except logout which requires authentication.
 *
 * @author Ali Haris Chishti
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and token management endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * @param registerRequestDto the registration details
     * @return authentication tokens upon successful registration
     */
    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account with the provided details.
                    
                    **Validation Rules:**
                    - Username: 4-20 characters, alphanumeric with underscores
                    - Password: Minimum 8 characters, must contain uppercase, lowercase, number, and special character
                    - Phone: Valid international format (e.g., +1234567890)
                    - First/Last name: Only letters, max 50 characters
                    
                    Upon successful registration, both access and refresh tokens are returned.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - validation failed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "status": "BAD_REQUEST",
                                                "message": "Validation failed",
                                                "timestamp": "2024-01-15T10:30:00",
                                                "path": "/api/v1/auth/register"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or phone number already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequestDto.class))
            )
            @RequestBody RegisterRequestDto registerRequestDto
    ) {
        log.info("Processing registration request for username: {}", registerRequestDto.getUsername());
        return ResponseEntity.status(CREATED).body(authService.register(registerRequestDto));
    }

    /**
     * Authenticate user and obtain tokens.
     *
     * @param loginRequestDto the login credentials
     * @return authentication tokens upon successful login
     * @throws BadCredentialsException if credentials are invalid
     */
    @Operation(
            summary = "User login",
            description = """
                    Authenticates a user with username and password.
                    
                    **Remember Me Option:**
                    - If `rememberMe` is true, refresh token validity is extended to 30 days
                    - If false, refresh token is valid for 24 hours
                    
                    Returns JWT access token (short-lived) and refresh token (long-lived).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            )
            @RequestBody LoginRequestDto loginRequestDto
    ) throws BadCredentialsException {
        log.info("Processing login request for username: {}", loginRequestDto.getUsername());
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshTokenDto the refresh token
     * @return new authentication tokens
     */
    @Operation(
            summary = "Refresh access token",
            description = """
                    Generates a new access token using a valid refresh token.
                    
                    Use this endpoint when the access token expires but the refresh token is still valid.
                    The refresh token is rotated (old one invalidated, new one issued) for security.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens refreshed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenDto.class))
            )
            @RequestBody RefreshTokenDto refreshTokenDto
    ) {
        log.debug("Processing token refresh request");
        return ResponseEntity.ok(authService.refresh(refreshTokenDto));
    }

    /**
     * Logout user and invalidate refresh token.
     *
     * @param refreshTokenDto the refresh token to invalidate
     * @return confirmation message
     */
    @Operation(
            summary = "User logout",
            description = """
                    Logs out the user by invalidating their refresh token.
                    
                    After logout:
                    - The refresh token cannot be used to generate new access tokens
                    - The current access token remains valid until it expires
                    - Client should discard both tokens
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Logged out successfully"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid refresh token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token to invalidate",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenDto.class))
            )
            @RequestBody RefreshTokenDto refreshTokenDto
    ) {
        log.info("Processing logout request");
        return ResponseEntity.ok(authService.logout(refreshTokenDto));
    }
}
