package com.larissafalcao.eventhub_api.service;

import com.larissafalcao.eventhub_api.dto.request.LoginRequest;
import com.larissafalcao.eventhub_api.dto.request.RegisterRequest;
import com.larissafalcao.eventhub_api.dto.response.AuthResponse;
import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.entity.Role;
import com.larissafalcao.eventhub_api.entity.User;
import com.larissafalcao.eventhub_api.exception.EmailAlreadyExistsException;
import com.larissafalcao.eventhub_api.repository.ParticipantRepository;
import com.larissafalcao.eventhub_api.repository.UserRepository;
import com.larissafalcao.eventhub_api.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register: creates user with participant and returns token")
    void registerCreatesUserWithParticipant() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@email.com")
                .password("password123")
                .build();
        Participant savedParticipant = Participant.builder()
                .id(10L)
                .name("Alice")
                .email("alice@email.com")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(participantRepository.save(any(Participant.class))).thenReturn(savedParticipant);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(1L)
                    .name(user.getName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .participant(user.getParticipant())
                    .build();
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("alice@email.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    @DisplayName("register: throws when email already exists")
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@email.com")
                .password("password123")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already registered: alice@email.com");

        verify(participantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: authenticates user and returns token")
    void loginReturnsTokenForAuthenticatedUser() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@email.com")
                .password("password123")
                .build();
        User authenticatedUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@email.com")
                .password("encoded-password")
                .role(Role.USER)
                .participant(Participant.builder()
                        .id(10L)
                        .name("Alice")
                        .email("alice@email.com")
                        .build())
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(authenticatedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("login: propagates authentication failure")
    void loginPropagatesAuthenticationFailure() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@email.com")
                .password("wrong-password")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("registerAdmin: creates admin user without participant and returns token")
    void registerAdminCreatesAdminWithoutParticipant() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Admin")
                .email("admin@email.com")
                .password("password123")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(1L)
                    .name(user.getName())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())
                    .build();
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.registerAdmin(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("admin@email.com");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerAdmin: throws when email already exists")
    void registerAdminThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Admin")
                .email("admin@email.com")
                .password("password123")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerAdmin(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
