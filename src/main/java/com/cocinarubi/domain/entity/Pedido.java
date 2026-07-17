package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.MetodoPago;
import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.DBConstants.TipoPedido;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root del sistema de órdenes. Registra toda venta del negocio.
 *
 * <p>Un pedido puede originarse desde el menú web ({@code WEB}) o ser creado
 * manualmente por un operador del dashboard ({@code COCINA}). Soporta tres
 * canales de entrega: recoger en tienda ({@code PICK_UP}), a domicilio
 * ({@code DOMICILIO}) o en mostrador ({@code MOSTRADOR}).</p>
 *
 * <p>El campo {@code impreso} controla la cola de tickets para la impresora
 * térmica ESC/POS (RF016/RF026). El campo {@code pago_cliente} permite calcular
 * el cambio cuando el método es {@code EFECTIVO}.</p>
 *
 * <p>La relación con {@link Cliente} es por {@code uuid_cliente} (VARCHAR, no INT),
 * lo que permite conservar el historial de pedidos aunque el cliente sea eliminado
 * (ON DELETE SET NULL).</p>
 *
 * <p>Relaciones hijas:
 * <ul>
 *   <li>{@link PedidoDomicilio} — datos de entrega (solo si tipo = DOMICILIO).</li>
 *   <li>{@link ComidaPedido} — líneas de platillos del menú principal.</li>
 *   <li>{@link DesayunoPedido} — líneas de platillos de desayuno.</li>
 *   <li>{@link ProductoCocinaPedido} — líneas de snacks, charolas y bebidas.</li>
 *   <li>{@link BasicoPedido} — líneas de paquetes básicos.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago_principal")
    private MetodoPago metodoPagoPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago_secundario")
    private MetodoPago metodoPagoSecundario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pedido")
    private TipoPedido tipoPedido;

    @Column(name = "fecha_expedicion_pedido")
    private LocalDateTime fechaExpedicionPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "pedido_creado_desde")
    private PedidoCreadoDesde pedidoCreadoDesde;

    @Column(name = "precio_final_orden")
    private BigDecimal precioFinalOrden;

    @Column(name = "pago_cliente_principal")
    private BigDecimal pagoCliente;

    @Column(name = "uuid_cliente", length = 45)
    private String uuidCliente;

    @Column(name = "impreso")
    private boolean impreso;


    @Builder.Default
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PedidoDomicilio pedidoDomicilio = null;

    @Builder.Default
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PedidoDomicilioCocina pedidoDomicilioCocina = null;

    @Builder.Default
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PedidoCocina pedidoCocina = null;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComidaPedido> comidasPedido = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DesayunoPedido> desayunosPedido = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoCocinaPedido> productosCocina = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BasicoPedido> basicosPedido = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Métodos helper para sincronización bidireccional
    // -------------------------------------------------------------------------

    public void addComidaPedido(ComidaPedido item) {
        item.setPedido(this);
        this.comidasPedido.add(item);
    }

    public void addDesayunoPedido(DesayunoPedido item) {
        item.setPedido(this);
        this.desayunosPedido.add(item);
    }

    public void addProductoCocinaPedido(ProductoCocinaPedido item) {
        item.setPedido(this);
        this.productosCocina.add(item);
    }

    public void addBasicoPedido(BasicoPedido item) {
        item.setPedido(this);
        this.basicosPedido.add(item);
    }
}
