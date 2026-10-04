package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Línea de pedido para un {@link Combo} (promoción). Sigue el mismo patrón que
 * {@link ProductoCocinaPedido}: FK al maestro + precio unitario congelado + cantidad.
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "combo_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo_pedido")
    private Integer idComboPedido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_combo")
    private Combo combo;

    @Column(name = "precio_unitario", precision = 7, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "cantidad")
    private Integer cantidad;
}
