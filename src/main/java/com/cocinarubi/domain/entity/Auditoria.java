package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Trail de auditoría para operaciones de escritura (POST/PUT/DELETE).
 * Se persiste de forma asíncrona por AuditoriaService.
 *
 * <p>datosAntes: se llena para PUT/PATCH con path variable id (snapshot pre-mutación);
 * queda null para POST, DELETE y endpoints body-only sin id en la ruta.</p>
 *
 * <p>datosDespues: se llena con el body retornado por el controller; queda null
 * cuando el endpoint retorna void.</p>
 */
@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private int idAuditoria;

    @Column(name = "tabla", nullable = false, length = 60)
    private String tabla;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private DBConstants.TipoOperacion tipoOperacion;

    @Column(name = "id_registro")
    private Integer idRegistro;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "datos_antes", columnDefinition = "JSON")
    private String datosAntes;

    @Column(name = "datos_despues", columnDefinition = "JSON")
    private String datosDespues;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = LocalDateTime.now();
    }
}
