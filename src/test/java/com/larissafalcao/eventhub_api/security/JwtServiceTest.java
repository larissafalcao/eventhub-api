package com.larissafalcao.eventhub_api.security;

import com.larissafalcao.eventhub_api.entity.Participant;
import com.larissafalcao.eventhub_api.entity.Role;
import com.larissafalcao.eventhub_api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "default-dev-secret-key-min-256-bits-long-for-hmac-sha";

    private final JwtService jwtService = new JwtService(SECRET, 60_000L);

    @Test
    @DisplayName("generateToken: returns valid token for user")
    void generateTokenReturnsValidToken() {
        User user = createUser("alice@email.com", Role.USER);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@email.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: returns false when token belongs to another user")
    void isTokenValidReturnsFalseForAnotherUser() {
        User alice = createUser("alice@email.com", Role.USER);
        User bob = createUser("bob@email.com", Role.ADMIN);
        String token = jwtService.generateToken(alice);

        boolean isValid = jwtService.isTokenValid(token, bob);

        assertThat(isValid).isFalse();
    }

    private static User createUser(String email, Role role) {
        return User.builder()
                .id(1L)
                .name("Alice")
                .email(email)
                .password("encoded-password")
                .role(role)
                .participant(Participant.builder()
                        .id(10L)
                        .name("Alice")
                        .email(email)
                        .build())
                .build();
    }
}
