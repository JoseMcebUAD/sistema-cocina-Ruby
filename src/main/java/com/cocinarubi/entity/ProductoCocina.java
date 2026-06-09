package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Producto de cocina adicional: snack, charola o bebida.
 *
 * <p>A diferencia de {@link Comida}, estos productos no tienen porción media/entera.
 * Las bebidas manejan precio diferenciado según el canal de entrega: {@code precio_domicilio}
 * para pedidos a domicilio, {@code precio_normal} para pick-up y mostrador (RF05).</p>
 *
 * <p>El {@code uuid_producto_cocina} es el identificador público para el menú web.</p>
 *
 * <p>Relaciones salientes: ninguna. Referenciado por {@link ProductoCocinaPedido}
 * y {@link FavoritoCliente} (FK lógica).</p>
 */
@Entity
@Table(name = "producto_cocina")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCocina {

    public enum Estatus { DISPONIBLE, NO_DISPONIBLE, AGOTADO }

    public enum TipoProducto { SNACK, CHAROLA, BEBIDA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_cocina")
    private Integer idProductoCocina;

    @Column(name = "uuid_producto_cocina", nullable = false, length = 45)
    private String uuidProductoCocina;

    @Column(name = "nombre_producto", nullable = false, length = 100)
    private String nombreProducto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio_domicilio", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioDomicilio;

    @Column(name = "precio_normal", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioNormal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private Estatus estatus;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false)
    private TipoProducto tipoProducto;
}
