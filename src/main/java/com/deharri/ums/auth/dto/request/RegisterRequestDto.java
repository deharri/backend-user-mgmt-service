package com.deharri.ums.auth.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.validation.annotation.ValidPassword;
import com.deharri.ums.validation.annotation.ValidPhoneNumber;
import com.deharri.ums.validation.annotation.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateClass
public class RegisterRequestDto {

    @ValidPhoneNumber
    private String phoneNumber;

    @ValidUsername
    private String username;

    @ValidPassword
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "First name must contain only letters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Last name must contain only letters"
    )
    private String lastName;

    @NotNull(message = "Role can not be null")
    private UserRole userRole;

    private boolean rememberMe;
}

