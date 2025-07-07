package com.deharri.ums.auth.mapper;


import com.deharri.ums.config.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthMapperHelper {

    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder;

    @Named("generateAccessToken")
    public String generateAccessToken(String username) {
        return jwtService.generateAccessToken(username);
    }

    @Named("generateRefreshToken")
    public String generateRefreshToken(String username, boolean rememberMe) {
        return jwtService.generateRefreshToken(username, rememberMe);
    }

    @Named(("encodePassword"))
    public  String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}
