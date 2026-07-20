package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Datos de entrega a domicilio para pedidos creados desde COCINA.
 *
 * <p>Extensión 1:1 de {@link Pedido}: solo existe cuando
 * {@code Pedido.pedidoCreadoDesde == COCINA} y {@code Pedido.tipoPedido == DOMICILIO}.
 * Comparte la PK con su pedido padre ({@code @MapsId}).</p>
 *
 * <p>A diferencia de {@link PedidoDomicilio} (flujo WEB), este registro guarda
 * un <strong>snapshot</strong> de {@code precio_tarifa} para preservar el costo
 * de envío aunque la tarifa de la ruta cambie en el futuro.</p>
 *
 * <p>La referencia a {@link RegistroCliente} permite trazar el historial de entregas
 * por cliente manual. La {@code direccion} y el {@code id_ruta} son los reales del
 * pedido y pueden diferir de los datos habituales del cliente.</p>
 */
@Entity
@Table(name = "pedido_domicilio_cocina")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDomicilioCocina {

    @Id
    @Column(name = "id_pedido")
    private Integer idPedido;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro_cliente", nullable = false)
    private RegistroCliente registroCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;

    @Column(name = "domicilio", nullable = false, length = 255)
    private String domicilio;

    @Column(name = "precio_tarifa", nullable = false, precision = 6, scale = 2)
    private BigDecimal precioTarifa;
}
