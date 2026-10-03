package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ingrediente de la receta de una comida. Pivote entre {@link Comida} y {@link Producto}.
 *
 * <p>Los campos {@code costo_unitario} y {@code costo_total} son SNAPSHOTS tomados en el
 * momento del alta/actualización (ver {@code RECETAS.md} sección 1.3). Preservan la historia
 * del costo cuando se registró la receta. Los endpoints de costeo escalado y cálculo de
 * faltante usan {@code Producto.costoUnitarioBase} en vivo para reflejar el costo actual.</p>
 *
 * <p>Capa: Entity — pivote de receta.</p>
 */
@Entity
@Table(
        name = "producto_receta",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_producto_receta_comida_producto",
                columnNames = {"id_comida", "id_producto"}
        )
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_receta")
    private Integer idProductoReceta;

    // Evita ciclos al serializar; la receta se consulta por idComida, no desde la Comida.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida", nullable = false)
    private Comida comida;

    // EAGER porque siempre se necesita nombre/costo/tipoMedida al pintar un ingrediente.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_usada", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidadUsada;

    @Column(name = "unidad_usada", length = 10, nullable = false)
    private String unidadUsada;

    @Column(name = "es_aproximado", nullable = false)
    private boolean esAproximado;

    @Column(name = "costo_unitario", precision = 10, scale = 4, nullable = false)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoTotal;
}
