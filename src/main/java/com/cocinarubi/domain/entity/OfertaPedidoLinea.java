package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoEntidadOferta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Línea del pedido que fue "consumida" por una {@link OfertaPedido}.
 *
 * <p>El campo {@code idLineaPedido} es una referencia polimórfica sin FK física
 * (igual que {@code combo_producto.id_producto} y {@code anuncio_producto.id_producto}):
 * su interpretación depende de {@code tipoEntidad}:
 * <ul>
 *   <li>{@code COMIDA} → PK en {@code comida_pedido}.</li>
 *   <li>{@code PRODUCTO_COCINA} → PK en {@code producto_cocina_pedido}.</li>
 *   <li>{@code BASICO} → PK en {@code basico_pedido}.</li>
 *   <li>{@code COMBO} → PK en {@code combo_pedido}.</li>
 * </ul>
 * </p>
 *
 * <p>Los campos {@code idProductoCatalogo} y {@code nombreProducto} están
 * denormalizados para construir la respuesta sin joins adicionales.</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "oferta_pedido_linea")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfertaPedidoLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta_pedido_linea")
    private Integer idOfertaPedidoLinea;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta_pedido")
    private OfertaPedido ofertaPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad")
    private TipoEntidadOferta tipoEntidad;

    /** PK de la línea en su tabla respectiva (sin FK física — polimórfico). */
    @Column(name = "id_linea_pedido")
    private Integer idLineaPedido;

    /** id de catálogo (idComida / idProductoCocina / idBasico / idCombo), denormalizado para respuesta. */
    @Column(name = "id_producto_catalogo")
    private Integer idProductoCatalogo;

    /** Snapshot del nombre del producto al momento de la orden. */
    @Column(name = "nombre_producto")
    private String nombreProducto;

    @Column(name = "cantidad_consumida")
    private int cantidadConsumida;

    /** Precio normal total de la línea (precioUnitario × cantidadConsumida) al momento de la orden. */
    @Column(name = "precio_normal_consumido")
    private java.math.BigDecimal precioNormalConsumido;
}
