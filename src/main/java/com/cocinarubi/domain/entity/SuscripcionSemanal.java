package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.OrigenSuscripcion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa una suscripción semanal de comidas contratada por un cliente.
 *
 * <p>El cliente paga una tarifa fija el lunes y tiene derecho a un número
 * determinado de comidas gratuitas por día (lunes a viernes) durante esa semana.
 * El campo {@code codigoRecuperacion} permite al cliente recuperar su suscripción
 * al cambiar de dispositivo o activarla manualmente.</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "suscripcion_semanal")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuscripcionSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion_semanal")
    private Integer idSuscripcionSemanal;

    @Column(name = "identificador_cliente", nullable = false, length = 150)
    private String identificadorCliente;

    @Column(name = "uuid_cliente", length = 45)
    private String uuidCliente;

    @Column(name = "semana_inicio", nullable = false)
    private LocalDate semanaInicio;

    @Column(name = "comidas_por_dia", nullable = false)
    private int comidasPorDia;

    @Column(name = "precio_suscripcion", nullable = false)
    private BigDecimal precioSuscripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(name = "codigo_recuperacion", nullable = false, unique = true, length = 30)
    private String codigoRecuperacion;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 10)
    private OrigenSuscripcion origen;
}
