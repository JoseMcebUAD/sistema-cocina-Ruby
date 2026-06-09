package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Línea de pedido para un platillo del menú de desayunos.
 *
 * <p>Los desayunos solo se venden en modalidad PICK_UP (L-S 7:00–11:00 h).
 * Al igual que {@link ComidaPedido}, almacena el precio en el momento de la
 * orden para preservar el historial aunque el catálogo cambie.</p>
 *
 * <p>⚠️ Nota: el encabezado de correcciones [C1] del esquema indica que se
 * debía agregar {@code tamano_porcion}, pero la columna no está presente en el
 * DDL actual. Ver BUG-2 en V1__crear_esquema.sql.</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link Pedido} — orden a la que pertenece.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Desayuno} — platillo de desayuno ordenado.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "desayuno_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesayunoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_desayuno_pedido")
    private Integer idDesayunoPedido;

    /** Pedido al que pertenece esta línea. Se ignora en JSON para evitar ciclos. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    /** Platillo de desayuno ordenado. No se puede eliminar mientras esté en pedidos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_desayuno", nullable = false)
    private Desayuno desayuno;

    /** Precio capturado al momento de la orden (histórico). */
    @Column(name = "precio", nullable = false, precision = 5, scale = 2)
    private BigDecimal precio;
}
