package com.larissafalcao.eventhub_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse implements Serializable {

    private Long id;
    private String name;
    private LocalDate date;
    private String location;
    private Integer capacity;

}
