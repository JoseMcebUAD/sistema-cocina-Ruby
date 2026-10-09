package com.cocinarubi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Snapshot de una tarifa especial aplicada a un pedido en el momento de su creación.
 * Tabla puente N-a-N entre {@link Pedido} y {@link TarifaEspecial}.
 *
 * <p>El campo {@code precioTarifa} guarda el valor vigente al crear el pedido; así,
 * si la tarifa cambia o se desactiva después, el historial del pedido permanece intacto.</p>
 *
 * <p>Capa: Entity — persistencia, sin lógica de negocio.</p>
 */
@Entity
@Table(name = "pedido_tarifa_especial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoTarifaEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarifa", nullable = false)
    private TarifaEspecial tarifaEspecial;

    @Column(name = "precio_tarifa", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioTarifa;
}
