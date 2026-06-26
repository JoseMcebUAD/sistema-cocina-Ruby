package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;

/**
 * Zona geográfica de reparto a domicilio.
 *
 * <p>Cada ruta delimita un polígono de entrega mediante el campo {@code boundary}
 * (tipo GEOMETRY de MySQL). El sistema usa {@code ST_Contains(boundary, POINT(lng, lat))}
 * en queries nativas para determinar a qué ruta pertenece la ubicación de un cliente.</p>
 *
 * <p>El número de rutas es fijo (RF011); no se crean ni eliminan desde el dashboard,
 * solo se activan/desactivan con {@code is_active}.</p>
 *
 * <p>Relaciones salientes: ninguna. Es referenciada por {@link TarifaEspecial},
 * {@link Cliente}, {@link PedidoDomicilio} y {@link PagoRepartidor}.</p>
 */
@Entity
@Table(name = "ruta")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "boundary", nullable = false, columnDefinition = "GEOMETRY")
    private Geometry boundary;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "tarifa_envio", nullable = false, precision = 6, scale = 2)
    private BigDecimal tarifaEnvio;

    @Column(name = "tiempo_estimado_min")
    private Integer tiempoEstimadoMin;
}
