package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Datos de entrega a domicilio de un {@link Pedido}.
 *
 * <p>Extensión 1:1 de {@link Pedido}: solo existe cuando
 * {@code Pedido.tipoPedido == DOMICILIO}. Comparte la PK con su pedido padre
 * ({@code @MapsId}), por lo que {@code id_pedido} es a la vez PK y FK.</p>
 *
 * <p>El campo {@code id_ruta} es la <strong>fuente de verdad</strong> de la zona
 * de entrega. No usar {@code Cliente#ruta} para calcular tarifas; ese es solo
 * un caché de sesión. El {@code codigo} corresponde a clientes con
 * {@link CodigoCliente} (fuera de rutas estándar).</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @OneToOne} con PK compartida a {@link Pedido}.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Ruta} — zona real de entrega.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "pedido_domicilio")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDomicilio {

    /** PK compartida con pedido. Se llena automáticamente por @MapsId. */
    @Id
    @Column(name = "id_pedido")
    private Integer idPedido;

    /** Pedido padre. Se ignora en JSON para evitar ciclos. */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    /**
     * Zona de reparto real donde se entregará el pedido. Fuente de verdad para
     * calcular tarifas. ON DELETE RESTRICT: no se puede eliminar una ruta si tiene
     * pedidos a domicilio asociados.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    /** Código especial si el cliente está fuera de las rutas estándar. Ver {@link CodigoCliente}. */
    @Column(name = "codigo", length = 255)
    private String codigo;

    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;
}
