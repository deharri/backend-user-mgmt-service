package com.deharri.ums.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtFilter Unit Tests")
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========================================================================
    // doFilterInternal() tests
    // ========================================================================

    @Nested
    @DisplayName("doFilterInternal()")
    class DoFilterInternalTests {

        @Test
        @DisplayName("Should continue filter chain without authentication when no Authorization header is present")
        void givenNoAuthHeader_whenDoFilter_thenContinueWithoutAuthentication() throws ServletException, IOException {
            // given
            when(request.getHeader("Authorization")).thenReturn(null);

            // when
            jwtFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService, userDetailsService);
        }

        @Test
        @DisplayName("Should continue filter chain without authentication when Authorization header is not Bearer")
        void givenNonBearerHeader_whenDoFilter_thenContinueWithoutAuthentication() throws ServletException, IOException {
            // given
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            // when
            jwtFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService, userDetailsService);
        }

        @Test
        @DisplayName("Should set authentication in SecurityContext when token is valid")
        void givenValidBearerToken_whenDoFilter_thenSetAuthentication() throws ServletException, IOException {
            // given
            String token = "valid.jwt.token";
            String username = "john_doe";
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("CONSUMER"));
            UserDetails userDetails = new User(username, "password", authorities);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsernameFromToken(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
            when(jwtService.isTokenValid(username, userDetails, token)).thenReturn(true);

            // when
            jwtFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
//            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
//                    .containsExactlyElementsOf(authorities);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue without authentication when token validation fails")
        void givenInvalidToken_whenDoFilter_thenContinueWithoutAuthentication() throws ServletException, IOException {
            // given
            String token = "invalid.jwt.token";
            String username = "john_doe";
            UserDetails userDetails = new User(username, "password", List.of());

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsernameFromToken(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
            when(jwtService.isTokenValid(username, userDetails, token)).thenReturn(false);

            // when
            jwtFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should skip token processing when SecurityContext already has authentication")
        void givenAlreadyAuthenticated_whenDoFilter_thenSkipTokenProcessing() throws ServletException, IOException {
            // given
            String token = "valid.jwt.token";
            String username = "john_doe";

            UsernamePasswordAuthenticationToken existingAuth =
                    new UsernamePasswordAuthenticationToken("existing_user", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsernameFromToken(token)).thenReturn(username);

            // when
            jwtFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("existing_user");
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(userDetailsService);
        }
    }
}
