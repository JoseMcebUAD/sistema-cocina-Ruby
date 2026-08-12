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
 * <p>La composición se modela vía {@link PaqueteProducto}, cuyas filas usan
 * un discriminador polimórfico {@code tipo_producto} + {@code id_producto} en
 * lugar de FKs físicas (mismo trade-off que {@code FavoritoCliente}).</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "paquete")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete")
    private Integer idPaquete;

    @Column(name = "precio")
    private BigDecimal precio;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "destacada")
    private boolean destacado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus")
    private Estatus estatus;

    // Cascade ALL + orphanRemoval: permite clear() + rebuild en update sin syncLineas manual.
    @OneToMany(mappedBy = "paquete",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaqueteProducto> productos = new ArrayList<>();

    public void addProducto(PaqueteProducto item) {
        item.setPaquete(this);
        this.productos.add(item);
    }
}
