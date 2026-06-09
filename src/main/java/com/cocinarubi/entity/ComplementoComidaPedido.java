package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Complemento seleccionado para una línea de comida dentro de un pedido.
 *
 * <p>Nodo hoja del árbol de pedido: {@link Pedido} → {@link ComidaPedido} →
 * {@code ComplementoComidaPedido}. Almacena el {@code precio_unitario} del
 * complemento al momento de la orden (precio histórico inmutable).</p>
 *
 * <p>Se elimina en cascada si se borra la {@link ComidaPedido} padre.
 * El {@link Complemento} referenciado no puede eliminarse mientras existan
 * líneas de pedido que lo usen (ON DELETE RESTRICT).</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link ComidaPedido} — línea de comida propietaria.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Complemento} — acompañamiento elegido.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "complemento_comida_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplementoComidaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_complemento_comida_pedido")
    private Integer idComplementoComidaPedido;

    /** Línea de comida a la que pertenece este complemento. Se ignora en JSON para evitar ciclos. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida_pedido", nullable = false)
    private ComidaPedido comidaPedido;

    /** Complemento elegido. No se puede eliminar del catálogo mientras esté en pedidos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_complemento", nullable = false)
    private Complemento complemento;

    /** Precio del complemento capturado al momento de la orden. Inmutable una vez creado. */
    @Column(name = "precio_unitario", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioUnitario;
}
