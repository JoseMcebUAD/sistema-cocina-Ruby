package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Línea de pedido para un snack, charola o bebida del catálogo de cocina.
 *
 * <p>A diferencia de {@link ComidaPedido}, este tipo de producto no tiene
 * porción media/entera, pero sí soporta {@code cantidad} (más de una unidad
 * del mismo producto en la misma línea). El {@code precio_unitario} se captura
 * al momento de la orden según si el pedido es a domicilio o no (RF05).</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link Pedido} — orden a la que pertenece.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link ProductoCocina} — producto ordenado.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "producto_cocina_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCocinaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_cocina_pedido")
    private Integer idProductoCocinaPedido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_cocina", nullable = false)
    private ProductoCocina productoCocina;

    @Column(name = "cantidad", nullable = false)
    private byte cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioUnitario;
}
