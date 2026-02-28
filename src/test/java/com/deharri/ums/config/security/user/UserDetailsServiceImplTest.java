package com.deharri.ums.config.security.user;

import com.deharri.ums.enums.UserRole;
import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Unit Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private CoreUser coreUser;

    @BeforeEach
    void setUp() {
        UserData userData = UserData.builder()
                .dataId(UUID.randomUUID())
                .phoneNumber("+1234567890")
                .userRoles(List.of(UserRole.ROLE_CONSUMER, UserRole.ROLE_WORKER))
                .build();

        coreUser = CoreUser.builder()
                .userId(UUID.randomUUID())
                .username("john_doe")
                .password("encodedPassword123")
                .firstName("John")
                .lastName("Doe")
                .userData(userData)
                .build();
    }

    // ========================================================================
    // loadUserByUsername() tests
    // ========================================================================

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should return UserPrincipal when username exists")
        void givenExistingUsername_whenLoadUserByUsername_thenReturnUserPrincipal() {
            // given
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));

            // when
            UserDetails userDetails = userDetailsService.loadUserByUsername("john_doe");

            // then
            assertThat(userDetails).isNotNull();
            assertThat(userDetails).isInstanceOf(UserPrincipal.class);
            assertThat(userDetails.getUsername()).isEqualTo("john_doe");
            assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when username does not exist")
        void givenNonExistingUsername_whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
            // given
            when(userRepository.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent_user"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("User with username: 'nonexistent_user' not found");

            verify(userRepository).findByUsername("nonexistent_user");
        }

        @Test
        @DisplayName("Should return correct authorities mapped from user roles")
        void givenUserWithRoles_whenLoadUserByUsername_thenReturnCorrectAuthorities() {
            // given
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));

            // when
            UserDetails userDetails = userDetailsService.loadUserByUsername("john_doe");

            // then
            List<String> authorityNames = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // UserPrincipal strips "ROLE_" prefix: ROLE_CONSUMER -> CONSUMER, ROLE_WORKER -> WORKER
            assertThat(authorityNames).containsExactlyInAnyOrder("CONSUMER", "WORKER");
            verify(userRepository).findByUsername("john_doe");
        }
    }
}
