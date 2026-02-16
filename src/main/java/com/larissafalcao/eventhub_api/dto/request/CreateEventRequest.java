package com.larissafalcao.eventhub_api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Event name must not be blank")
    private String name;

    @NotNull(message = "Event date must not be null")
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate date;

    @NotBlank(message = "Event location must not be blank")
    private String location;

    @NotNull(message = "Event capacity must not be null")
    @Positive(message = "Event capacity must be positive")
    private Integer capacity;

}
