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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            ParticipantRepository participantRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Participant participant = participantRepository.save(Participant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build());

        User user = userRepository.save(User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .participant(participant)
                .build());

        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = userRepository.save(User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build());

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Unexpected authentication principal type");
        }
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
