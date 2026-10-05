package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado raíz del módulo de promociones. Agrupa productos de varias tablas
 * ({@link Comida}, {@link Desayuno}, {@link Complemento}, {@link ProductoCocina})
 * en una sola oferta con precio propio.
 *
 * <p>La composición se modela vía {@link ComboProducto}, cuyas filas usan
 * un discriminador polimórfico {@code tipo_producto} + {@code id_producto} en
 * lugar de FKs físicas (mismo trade-off que {@code FavoritoCliente}).</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "combo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo")
    private Integer idCombo;

    @Column(name = "precio")
    private BigDecimal precio;

    @Column(name = "descripcion", length = 400)
    private String descripcion;

    @Column(name = "destacada")
    private boolean destacado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus")
    private Estatus estatus;

    @Column(name = "descuento_activo")
    private boolean descuentoActivo = false;

    @Column(name = "descripcion_descuento", length = 400)
    private String descripcionDescuento;

    @Column(name = "precio_descuento")
    private BigDecimal precioDescuento;

    // Cascade ALL + orphanRemoval: permite clear() + rebuild en update sin syncLineas manual.
    @OneToMany(mappedBy = "combo",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<ComboProducto> productos = new ArrayList<>();

    public void addProducto(ComboProducto item) {
        item.setCombo(this);
        this.productos.add(item);
    }
}
