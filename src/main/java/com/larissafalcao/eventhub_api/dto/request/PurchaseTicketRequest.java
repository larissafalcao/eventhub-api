package com.larissafalcao.eventhub_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class PurchaseTicketRequest {

    @NotNull(message = "Participant id must not be null")
    @Positive(message = "Participant id must be positive")
    private Long participantId;
}
