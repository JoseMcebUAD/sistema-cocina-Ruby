package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Categoría de clasificación de {@link ProductoCocina} (por ejemplo BEBIDA, SNACK, POSTRE).
 *
 * <p>Reemplazó al enum estático {@code DBConstants.TipoProducto} eliminado en Fase 2,
 * permitiendo que el usuario amplíe libremente el catálogo desde la vista de configuración.</p>
 *
 * <p>Capa: Entity — persistencia. Unicidad de {@code nombre} enforced en BD
 * (constraint {@code uq_categoria_nombre}).</p>
 */
@Entity
@Table(name = "categoria")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre")
    private String nombre;

    // Relación inversa con Subcategoria. Cascada completa + orphanRemoval:
    // guardar una Categoria persiste sus subcategorias, y remover una subcategoria
    // de la lista la elimina de la BD. LAZY para no cargarlas por defecto.
    @OneToMany(mappedBy = "categoria",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<Subcategoria> subcategorias = new ArrayList<>();
}
