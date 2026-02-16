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
public class CreateParticipantRequest {

    @NotBlank(message = "Participant name must not be blank")
    private String name;

    @NotBlank(message = "Participant email must not be blank")
    @Email(message = "Participant email must be valid")
    private String email;
}
