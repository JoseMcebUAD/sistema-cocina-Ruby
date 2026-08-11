package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Producto de cocina adicional: snack, charola, bebida, postre, o cualquier
 * categoría definida por el usuario a partir de Fase 2.
 *
 * <p>A diferencia de {@link Comida}, estos productos no tienen porción media/entera.
 * Las bebidas manejan precio diferenciado según el canal de entrega: {@code precio_domicilio}
 * para pedidos a domicilio, {@code precio_normal} para pick-up y mostrador (RF05).</p>
 *
 * <p>El {@code uuid_producto_cocina} es el identificador público para el menú web.</p>
 *
<<<<<<< HEAD
 * <p>Relaciones salientes: ninguna. Referenciado por {@link ProductoCocinaPedido}
=======
 * <p>Relaciones salientes: {@code @ManyToOne} LAZY a {@link Categoria} (clasificación
 * principal), {@code @ManyToMany} LAZY a {@link Subcategoria} vía tabla puente
 * {@code producto_cocina_subcategoria} (desglose fino, 0..N). Todas las subcategorías
 * asignadas deben pertenecer a la misma categoría — validado en el service.</p>
>>>>>>> feat/categorias
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    // Muchos-a-muchos con tabla puente. LAZY porque solo se hidrata bajo demanda;
    // el service usa findAllById para validar consistencia (misma categoría).
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "producto_cocina_subcategoria",
        joinColumns = @JoinColumn(name = "id_producto_cocina"),
        inverseJoinColumns = @JoinColumn(name = "id_subcategoria"))
    @Builder.Default
    private List<Subcategoria> subcategorias = new ArrayList<>();
}
