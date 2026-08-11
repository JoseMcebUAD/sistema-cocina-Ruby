package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Línea de pedido para un {@link Paquete} (promoción). Sigue el mismo patrón que
 * {@link ProductoCocinaPedido}: FK al maestro + precio unitario congelado + cantidad.
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "paquete_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaquetePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete_pedido")
    private Integer idPaquetePedido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paquete")
    private Paquete paquete;

    @Column(name = "precio_unitario", precision = 7, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "cantidad")
    private Integer cantidad;
}
