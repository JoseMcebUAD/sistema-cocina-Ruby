package com.cocinarubi.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final String errorCode;
    private final T data;

    private ApiResponse(int status, String message, String errorCode, T data) {
        this.timestamp = LocalDateTime.now(ZoneId.of("America/Merida"));
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static <T> ApiResponse<T> exito(int status, String message, T data) {
        return new ApiResponse<>(status, message, null, data);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, null, null);
    }

    public static ApiResponse<Void> error(int status, String message, String errorCode) {
        return new ApiResponse<>(status, message, errorCode, null);
    }

    public static <T> ApiResponse<T> errorConDatos(int status, String message, T data) {
        return new ApiResponse<>(status, message, null, data);
    }
}
