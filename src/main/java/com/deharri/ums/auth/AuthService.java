package com.deharri.ums.auth;

import com.deharri.ums.annotations.ValidateArguments;
import com.deharri.ums.auth.dto.LoginRequestDto;
import com.deharri.ums.auth.dto.RefreshTokenDto;
import com.deharri.ums.auth.dto.RegisterRequestDto;
import com.deharri.ums.auth.dto.AuthResponseDto;
import com.deharri.ums.auth.mapper.AuthMapper;
import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.config.security.jwt.RefreshToken;
import com.deharri.ums.config.security.jwt.RefreshTokenRepository;
import com.deharri.ums.enums.ExceptionMessage;
import com.deharri.ums.error.exception.AuthenticationException;
import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthMapper authMapper;

    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder;


    @ValidateArguments
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {
        CoreUser coreUser = authMapper.registerRequestDtoToCoreUser(registerRequestDto);
        coreUser = userRepository.save(coreUser);
        return authMapper.coreUserToAuthResponseDto(coreUser, registerRequestDto.isRememberMe());
    }

    @ValidateArguments
    public AuthResponseDto login(LoginRequestDto loginRequestDto) throws BadCredentialsException {
        CoreUser coreUser = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new AuthenticationException(ExceptionMessage.USER_NOT_FOUND_WITH_USERNAME));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), coreUser.getPassword())) {
            throw new AuthenticationException(ExceptionMessage.INCORRECT_PASSWORD);
        }

        return authMapper.coreUserToAuthResponseDto(coreUser, loginRequestDto.isRememberMe());
    }


    public AuthResponseDto refresh(RefreshTokenDto refreshTokenDto) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenDto.getToken())
                .orElseThrow(() -> new AuthenticationException(ExceptionMessage.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired() || !jwtService.extractUsernameFromToken(refreshToken.getToken()).equals(refreshToken.getUsername())) {
            throw new AuthenticationException(ExceptionMessage.REFRESH_TOKEN_EXPIRED);
        }

        return authMapper.refreshTokenToAuthResponseDto(refreshToken);
    }
}
