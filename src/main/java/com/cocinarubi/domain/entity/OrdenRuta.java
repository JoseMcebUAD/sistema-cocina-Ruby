package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Representa un grupo de rutas de reparto con su ventana horaria estimada de llegada.
 * Capa: Entity — mapeo JPA de la tabla orden_ruta.
 */
@Entity
@Table(name = "orden_ruta")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_ruta")
    private Integer idOrdenRuta;

    @Column(name = "tiempo_estimado_min")
    private Integer tiempoEstimadoMin;

    @Column(name = "hora_llegada_desde")
    private LocalTime horaLlegadaDesde;

    @Column(name = "hora_llegada_hasta")
    private LocalTime horaLlegadaHasta;
}
