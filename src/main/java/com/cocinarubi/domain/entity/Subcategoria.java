package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Subcategoría asociada a una {@link Categoria}. Permite un desglose fino de los productos
 * (por ejemplo dentro de BEBIDA: "Fría", "Caliente"; dentro de SNACK: "Salado", "Dulce").
 *
 * <p>Capa: Entity — persistencia. Unicidad de {@code nombre} por categoría enforced en BD
 * (constraint {@code uq_subcategoria_nombre_por_categoria}).</p>
 */
@Entity
@Table(name = "subcategoria")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subcategoria")
    private Integer idSubcategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "nombre")
    private String nombre;
}
