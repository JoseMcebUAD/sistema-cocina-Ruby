package com.cocinarubi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ErrorCode tipoErrorCode;

    public BusinessException(String mensaje, HttpStatus httpStatus) {
        super(mensaje);
        this.httpStatus = httpStatus;
        this.tipoErrorCode = ErrorCode.GENERICO;
    }

    public BusinessException(String mensaje, HttpStatus httpStatus, ErrorCode tipoErrorCode) {
        super(mensaje);
        this.httpStatus = httpStatus;
        this.tipoErrorCode = tipoErrorCode;
    }
}
