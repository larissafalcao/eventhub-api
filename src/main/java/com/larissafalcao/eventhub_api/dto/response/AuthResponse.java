package com.larissafalcao.eventhub_api.dto.response;

import com.larissafalcao.eventhub_api.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private Role role;
}
