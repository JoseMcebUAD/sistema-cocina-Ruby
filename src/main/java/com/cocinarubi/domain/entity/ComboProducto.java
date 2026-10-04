package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoLineaCombo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Línea de composición de un {@link Combo}. El par ({@code tipoProducto}, {@code idProducto})
 * es un discriminador polimórfico que apunta a una fila de {@code comida}, {@code desayuno},
 * {@code complemento} o {@code producto_cocina}.
 *
 * <p>No hay FK física porque {@code id_producto} referencia distintas tablas según el tipo;
 * la integridad se valida en el service (mismo patrón que {@code FavoritoCliente}).</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "combo_producto",
       uniqueConstraints = @UniqueConstraint(name = "uq_combo_producto",
               columnNames = {"id_combo", "tipo_producto", "id_producto"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo_producto")
    private Integer idComboProducto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_combo")
    private Combo combo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto")
    private TipoLineaCombo tipoProducto;

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "cantidad")
    private Integer cantidad;
}
