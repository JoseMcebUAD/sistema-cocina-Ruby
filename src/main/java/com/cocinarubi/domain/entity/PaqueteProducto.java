package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoLineaPaquete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Línea de composición de un {@link Paquete}. El par ({@code tipoProducto}, {@code idProducto})
 * es un discriminador polimórfico que apunta a una fila de {@code comida}, {@code desayuno},
 * {@code complemento} o {@code producto_cocina}.
 *
 * <p>No hay FK física porque {@code id_producto} referencia distintas tablas según el tipo;
 * la integridad se valida en el service (mismo patrón que {@code FavoritoCliente}).</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "paquete_producto",
       uniqueConstraints = @UniqueConstraint(name = "uq_paquete_producto",
               columnNames = {"id_paquete", "tipo_producto", "id_producto"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaqueteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete_producto")
    private Integer idPaqueteProducto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paquete")
    private Paquete paquete;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto")
    private TipoLineaPaquete tipoProducto;

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "cantidad")
    private Integer cantidad;
}
