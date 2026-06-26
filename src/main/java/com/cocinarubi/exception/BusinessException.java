package com.cocinarubi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;

    public BusinessException(String mensaje, HttpStatus httpStatus) {
        super(mensaje);
        this.httpStatus = httpStatus;
    }
}
