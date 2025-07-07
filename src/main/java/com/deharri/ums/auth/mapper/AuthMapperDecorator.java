package com.deharri.ums.auth.mapper;

import com.deharri.ums.auth.dto.LoginRequestDto;
import com.deharri.ums.auth.dto.RegisterRequestDto;
import com.deharri.ums.auth.dto.AuthResponseDto;
import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.config.security.jwt.RefreshTokenRepository;
import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthMapperDecorator implements AuthMapper{

    @Qualifier("delegate")
    private AuthMapper delegate;
    @Autowired
    public void setDelegate(AuthMapper delegate) {
        this.delegate = delegate;
    }

    private JwtService jwtService;
    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private UserRepository userRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    public void setRefreshTokenRepository(RefreshTokenRepository refreshTokenRepository) {

    }


//    @Override
//    public AuthResponseDto loginRequestDtoToAuthResponseDto(LoginRequestDto loginRequestDto) {
//        AuthResponseDto dto = new AuthResponseDto();
//
//        dto.setAccessToken(jwtService.generateAccessToken(
//                loginRequestDto.getUsername(),
//                loginRequestDto.getPassword()
//        ));
//        dto.setUuid(userRepository.getUuidByUsername(loginRequestDto.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found")));
//        dto.setRefreshToken(jwtService.generateRefreshToken(
//                loginRequestDto.getUsername(),
//                loginRequestDto.isRememberMe()
//        ));
//
//        return dto;
//    }
//
//
//    @Override
//    public AuthResponseDto registerRequestDtoToAuthResponseDto(RegisterRequestDto registerRequestDto) {
//        CoreUser coreUser = delegate.registerRequestDtoToCoreUser(registerRequestDto);
//        coreUser = userRepository.save(coreUser);
//
//        AuthResponseDto dto = delegate.coreUserToAuthResponseDto(coreUser);
//        dto.setUuid(coreUser.getUuid());
//        dto.setAccessToken(jwtService.generateAccessToken(
//                coreUser.getUsername(),
//                coreUser.getPassword()
//        ));
//        dto.setRefreshToken(jwtService.generateRefreshToken(
//                coreUser.getUsername(),
//                registerRequestDto.isRememberMe()
//        ));
//        return dto;
//    }
}
