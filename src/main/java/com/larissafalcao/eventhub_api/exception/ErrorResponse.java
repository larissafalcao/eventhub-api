package com.larissafalcao.eventhub_api.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ErrorResponse {

    private String code;
    private String message;
    private List<String> errors;
    private Instant timestamp;

    public ErrorResponse(String code, String message, List<String> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now();
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String code, String message, List<String> errors) {
        return new ErrorResponse(code, message, errors);
    }

}
