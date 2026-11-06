package com.cocinarubi.domain.entity;

import com.cocinarubi.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de excepción no controlada capturada por el handler global.
 * Capa: Entity — tabla de auditoría técnica de errores en producción.
 */
@Entity
@Table(name = "error_log")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_error")
    private Integer idError;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "metodo_http", nullable = false, length = 10)
    private String metodoHttp;

    @Column(name = "tipo_error", nullable = false)
    private String tipoError;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    /** Stack trace serializado, truncado a 4 000 chars para respetar el límite práctico de TEXT en alto volumen. */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = LocalDateTime.now(Constants.ZONA_MERIDA);
    }
}
