package com.deharri.ums.auth.mapper;


import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.config.security.jwt.refresh.RefreshTokenService;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.user.UserService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthMapperHelper {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    private final BCryptPasswordEncoder passwordEncoder;

    @Named("generateAccessToken")
    public String generateAccessToken(String username) {
        return jwtService.generateAccessToken(username, userService.getUserRoles(username));
    }

    @Named("generateRefreshToken")
    public String generateRefreshToken(String username, boolean rememberMe) {
        String tokenString = jwtService.generateRefreshToken(username, rememberMe);
        refreshTokenService.saveToken(tokenString, username);
        return tokenString;
    }

    @Named(("encodePassword"))
    public  String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}
