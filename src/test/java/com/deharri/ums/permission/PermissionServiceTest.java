package com.deharri.ums.permission;

import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PermissionService permissionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========================================================================
    // getLoggedInUsersUsername() tests
    // ========================================================================

    @Nested
    @DisplayName("getLoggedInUsersUsername()")
    class GetLoggedInUsersUsernameTests {

        @Test
        @DisplayName("Should return username when principal is UserDetails")
        void givenUserDetailsInContext_whenGetLoggedInUsersUsername_thenReturnUsername() {
            // given
            UserDetails userDetails = new User("john_doe", "password", Collections.emptyList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // when
            String username = permissionService.getLoggedInUsersUsername();

            // then
            assertThat(username).isEqualTo("john_doe");
        }

        @Test
        @DisplayName("Should return string representation when principal is a plain String")
        void givenStringPrincipalInContext_whenGetLoggedInUsersUsername_thenReturnString() {
            // given
            String principalString = "string_principal_user";
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principalString, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // when
            String username = permissionService.getLoggedInUsersUsername();

            // then
            assertThat(username).isEqualTo("string_principal_user");
        }
    }

    // ========================================================================
    // getLoggedInUser() tests
    // ========================================================================

    @Nested
    @DisplayName("getLoggedInUser()")
    class GetLoggedInUserTests {

        @BeforeEach
        void setUpAuthentication() {
            UserDetails userDetails = new User("john_doe", "password", Collections.emptyList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("Should return CoreUser when logged-in user exists in the repository")
        void givenValidUsername_whenGetLoggedInUser_thenReturnCoreUser() {
            // given
            CoreUser coreUser = CoreUser.builder()
                    .userId(UUID.randomUUID())
                    .username("john_doe")
                    .password("encodedPassword123")
                    .firstName("John")
                    .lastName("Doe")
                    .userData(UserData.builder()
                            .dataId(UUID.randomUUID())
                            .phoneNumber("+1234567890")
                            .build())
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));

            // when
            CoreUser result = permissionService.getLoggedInUser();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john_doe");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when logged-in user is not found in the repository")
        void givenInvalidUsername_whenGetLoggedInUser_thenThrowUsernameNotFoundException() {
            // given
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> permissionService.getLoggedInUser())
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("User not found: john_doe");

            verify(userRepository).findByUsername("john_doe");
        }
    }
}
