package com.fundflow.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;      // short reason phrase, e.g. "Not Found"
    private String message;    // human-readable detail
    private String path;
    private Map<String, String> fieldErrors; // only present for validation failures
}
