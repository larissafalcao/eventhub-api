package com.larissafalcao.eventhub_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {

    @NotBlank(message = "User email must not be blank")
    @Email(message = "User email must be valid")
    private String email;

    @NotBlank(message = "User password must not be blank")
    private String password;
}
