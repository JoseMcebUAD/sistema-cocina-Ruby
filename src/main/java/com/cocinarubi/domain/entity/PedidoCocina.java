package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Nombre del cliente para pedidos COCINA de tipo PICK_UP o MOSTRADOR.
 *
 * <p>Extensión 1:1 de {@link Pedido}: solo existe cuando
 * {@code Pedido.pedidoCreadoDesde == COCINA} y
 * {@code Pedido.tipoPedido} es {@code PICK_UP} o {@code MOSTRADOR}.
 * Comparte la PK con su pedido padre ({@code @MapsId}).</p>
 *
 * <p>{@code nombre_cliente} es nullable: el operador puede no conocer el nombre
 * al momento de crear el pedido.</p>
 */
@Entity
@Table(name = "pedido_cocina")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCocina {

    @Id
    @Column(name = "id_pedido")
    private Integer idPedido;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @Column(name = "nombre_cliente", length = 255)
    private String nombreCliente;
}
