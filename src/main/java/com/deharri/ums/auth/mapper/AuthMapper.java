package com.deharri.ums.auth.mapper;

import com.deharri.ums.auth.dto.response.AuthResponseDto;
import com.deharri.ums.auth.dto.request.LoginRequestDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.config.security.jwt.refresh.RefreshToken;
import com.deharri.ums.user.entity.CoreUser;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = AuthMapperHelper.class
)
public interface AuthMapper {

    @Mapping(source = "userRole", target = "userData.userRole")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    CoreUser registerRequestDtoToCoreUser(RegisterRequestDto registerRequestDto);

    AuthResponseDto loginRequestDtoToAuthResponseDto(LoginRequestDto loginRequestDto);

    AuthResponseDto registerRequestDtoToAuthResponseDto(RegisterRequestDto registerRequestDto);

    @Mapping(source = "username", target = "accessToken", qualifiedByName = "generateAccessToken")
    @Mapping(target = "refreshToken", expression = "java(authMapperHelper.generateRefreshToken(coreUser.getUsername(), rememberMe))")
    AuthResponseDto coreUserToAuthResponseDto(CoreUser coreUser, @Context boolean rememberMe);

    @Mapping(source = "token", target = "refreshToken")
    @Mapping(source = "username", target = "accessToken", qualifiedByName = "generateAccessToken")
    AuthResponseDto refreshTokenToAuthResponseDto(RefreshToken refreshToken);

}
