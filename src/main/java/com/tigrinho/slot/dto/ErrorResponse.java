package com.tigrinho.slot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;
    private Map<String, Object> details;

    public ErrorResponse(LocalDateTime timestamp, int status, String message, String path, Map<String, Object> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.details = details;
    }
}
