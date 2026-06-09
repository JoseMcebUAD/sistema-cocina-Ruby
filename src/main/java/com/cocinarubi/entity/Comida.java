package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Platillo del menú principal de Cocina Ruby.
 *
 * <p>Cada comida puede servirse en dos tamaños: media porción ({@code precio_media})
 * o porción entera ({@code precio_entera}). El campo {@code destacado} permite
 * que el operador priorice visualmente ciertos platillos en el carrito web.</p>
 *
 * <p>El {@code uuid_comida} es el identificador público que se usa en el menú web
 * (en lugar del {@code id_comida} interno) para evitar exponer secuencias de DB.</p>
 *
 * <p>Relaciones salientes: ninguna. Es referenciada por {@link ComidaPedido},
 * {@link Basico} e {@link InventarioComida}.</p>
 */
@Entity
@Table(name = "comida")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comida {

    /** Estado de disponibilidad del platillo en el menú web. */
    public enum Estatus { DISPONIBLE, NO_DISPONIBLE, AGOTADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comida")
    private Integer idComida;

    @Column(name = "uuid_comida", nullable = false, length = 45)
    private String uuidComida;

    @Column(name = "nombre_comida", nullable = false, length = 255)
    private String nombreComida;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio_media", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMedia;

    @Column(name = "precio_entera", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioEntera;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private Estatus estatus;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;
}
