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

    @Mapping(source = "phoneNumber", target = "userData.phoneNumber")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    @Mapping(target = "userId", ignore = true)
    CoreUser registerRequestDtoToCoreUser(RegisterRequestDto registerRequestDto);

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    AuthResponseDto loginRequestDtoToAuthResponseDto(LoginRequestDto loginRequestDto);

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    AuthResponseDto registerRequestDtoToAuthResponseDto(RegisterRequestDto registerRequestDto);

    @Mapping(target = "accessToken", expression = "java(authMapperHelper.generateAccessToken(coreUser.getUserId(), coreUser.getUsername()))")
    @Mapping(target = "refreshToken", expression = "java(authMapperHelper.generateRefreshToken(coreUser.getUsername(), rememberMe))")
    AuthResponseDto coreUserToAuthResponseDto(CoreUser coreUser, @Context boolean rememberMe);

    @Mapping(source = "token", target = "refreshToken")
    @Mapping(source = "username", target = "accessToken", qualifiedByName = "generateAccessTokenWithUsername")
    AuthResponseDto refreshTokenToAuthResponseDto(RefreshToken refreshToken);

}
