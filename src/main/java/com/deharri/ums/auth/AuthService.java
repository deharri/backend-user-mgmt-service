package com.deharri.ums.auth;

import com.deharri.ums.annotations.ValidateArguments;
import com.deharri.ums.auth.dto.request.LoginRequestDto;
import com.deharri.ums.auth.dto.request.RefreshTokenDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.auth.dto.response.AuthResponseDto;
import com.deharri.ums.auth.mapper.AuthMapper;
import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.config.security.jwt.refresh.RefreshToken;
import com.deharri.ums.config.security.jwt.refresh.RefreshTokenService;
import com.deharri.ums.enums.ExceptionMessage;
import com.deharri.ums.error.exception.AuthenticationException;
import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final AuthMapper authMapper;

    private final RefreshTokenService refreshTokenService;

    private final BCryptPasswordEncoder passwordEncoder;


    @ValidateArguments
    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequestDto) {
        CoreUser coreUser = authMapper.registerRequestDtoToCoreUser(registerRequestDto);
        coreUser = userRepository.save(coreUser);
        return authMapper.coreUserToAuthResponseDto(coreUser, registerRequestDto.isRememberMe());
    }

    @ValidateArguments
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequestDto) throws BadCredentialsException {
        CoreUser coreUser = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new AuthenticationException(ExceptionMessage.USER_NOT_FOUND_WITH_USERNAME));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), coreUser.getPassword())) {
            throw new AuthenticationException(ExceptionMessage.INCORRECT_PASSWORD);
        }

        return authMapper.coreUserToAuthResponseDto(coreUser, loginRequestDto.isRememberMe());
    }


    public AuthResponseDto refresh(RefreshTokenDto refreshTokenDto) {
        RefreshToken refreshToken = refreshTokenService.getRefreshTokenIfExists(refreshTokenDto.getToken());
        return authMapper.refreshTokenToAuthResponseDto(refreshToken);
    }

    public Map<String, String> logout(RefreshTokenDto refreshTokenDto) {
        RefreshToken refreshToken = refreshTokenService.getRefreshTokenIfExists(refreshTokenDto.getToken());
        refreshTokenService.deleteToken(refreshToken);
        return Map.of("message", String.format("User: %s logged out successfully!", refreshToken.getUsername()));
    }

}
