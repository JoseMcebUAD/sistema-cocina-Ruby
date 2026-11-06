package com.cocinarubi.exception;

import com.cocinarubi.domain.entity.Usuario;
import com.cocinarubi.domain.service.ErrorLogService;
import com.cocinarubi.presentation.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorLogService errorLogService;

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> manejarRutaNoEncontrada(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, "La ruta solicitada no existe."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> manejarJsonInvalido(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        errorLogService.registrar(request.getRequestURI(), request.getMethod(), ex, extraerIdUsuario());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "JSON inválido o tipo de dato incorrecto en la solicitud"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> manejarTipoArgumento(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        errorLogService.registrar(request.getRequestURI(), request.getMethod(), ex, extraerIdUsuario());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Parámetro inválido: '" + ex.getName() + "' no es del tipo esperado"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> manejarValidacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, errores));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ApiResponse<Void>> manejarStockInsuficiente(
            StockInsuficienteException ex, HttpServletRequest request) {
        errorLogService.registrar(request.getRequestURI(), request.getMethod(), ex, extraerIdUsuario());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> manejarNegocio(
            BusinessException ex, HttpServletRequest request) {
        log.warn("[{}] {}", ex.getTipoErrorCode().name(), ex.getMessage());
        // ErrorLogService filtra BusinessException internamente — no se persiste
        errorLogService.registrar(request.getRequestURI(), request.getMethod(), ex, extraerIdUsuario());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getHttpStatus().value(), ex.getMessage(), ex.getTipoErrorCode().name()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> manejarGenerico(
            Exception ex, HttpServletRequest request) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        errorLogService.registrar(request.getRequestURI(), request.getMethod(), ex, extraerIdUsuario());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Error interno del servidor. Contacte al administrador."));
    }

    /** Lee el idUsuario del SecurityContext en el hilo del request, antes del dispatch async. */
    private Integer extraerIdUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario u) return u.getIdUsuario();
        return null;
    }
}
