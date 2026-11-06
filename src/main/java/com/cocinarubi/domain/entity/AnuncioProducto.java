package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoProductoAnuncio;
import jakarta.persistence.*;
import lombok.*;

/**
 * Asociación polimórfica entre un {@link Anuncio} y un producto del catálogo.
 * Capa: Entity — sin FK directa a las tablas de producto (mismo patrón que {@code ComboProducto}).
 */
@Entity
@Table(name = "anuncio_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnuncioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anuncio_producto")
    private Integer idAnuncioProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_anuncio", nullable = false)
    private Anuncio anuncio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto")
    private TipoProductoAnuncio tipoProducto;

    @Column(name = "id_producto")
    private Integer idProducto;
}
