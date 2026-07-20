package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyección de solo lectura sobre la VIEW {@code vista_resumen_pedido} (V13).
 * Consolida pedidos WEB/COCINA y PICK_UP/DOMICILIO/MOSTRADOR con el nombre de cliente,
 * ruta, domicilio y tarifa ya resueltos por COALESCE en la vista.
 */
@Entity
@Immutable
@Table(name = "vista_resumen_pedido")
@Getter
@NoArgsConstructor
public class VistaResumenPedido {

    @Id
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "impreso")
    private Boolean impreso;

    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago_principal")
    private MetodoPago metodoPagoPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago_secundario")
    private MetodoPago metodoPagoSecundario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pedido")
    private TipoPedido tipoPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "pedido_creado_desde")
    private PedidoCreadoDesde pedidoCreadoDesde;

    @Column(name = "fecha_expedicion_pedido")
    private LocalDateTime fechaExpedicionPedido;

    @Column(name = "precio_final_orden")
    private BigDecimal precioFinalOrden;

    @Column(name = "pago_cliente_principal")
    private BigDecimal pagoClientePrincipal;

    @Column(name = "ruta")
    private String ruta;

    @Column(name = "domicilio")
    private String domicilio;

    @Column(name = "precio_tarifa")
    private BigDecimal precioTarifa;
}
