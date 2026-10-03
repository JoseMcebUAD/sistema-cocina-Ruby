package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoMedida;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Producto / ingrediente del catálogo de cocina.
 *
 * <p>Representa un insumo con el que se arman recetas (sal, carne, pollo, etc.).
 * Guarda cómo se presenta en el mercado (bolsa de 500 g, caja, etc.) y su costo,
 * del cual se deriva {@code costo_unitario_base} para costear recetas.</p>
 *
 * <p>Para productos de pieza con peso variable (pechuga de pollo), se guarda
 * {@code peso_promedio_pieza} para estimar costos en recetas que miden en piezas.</p>
 *
 * <p>Capa: Entity — modelo persistente del catálogo de ingredientes.</p>
 */
@Entity
@Table(name = "producto")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medida", nullable = false)
    private TipoMedida tipoMedida;

    @Column(name = "unidad_base", length = 10, nullable = false)
    private String unidadBase;

    @Column(name = "peso_promedio_pieza", precision = 10, scale = 2)
    private BigDecimal pesoPromedioPieza;

    @Column(name = "contenido_presentacion", precision = 10, scale = 2, nullable = false)
    private BigDecimal contenidoPresentacion;

    @Column(name = "unidad_presentacion", length = 10, nullable = false)
    private String unidadPresentacion;

    @Column(name = "costo_presentacion", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoPresentacion;

    @Column(name = "costo_unitario_base", precision = 10, scale = 4, nullable = false)
    private BigDecimal costoUnitarioBase;
}
