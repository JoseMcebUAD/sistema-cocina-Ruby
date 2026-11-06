package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.ModoMatchOferta;
import com.cocinarubi.DBConstants.TipoComida;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Slot de matching de una {@link Oferta}: define cuántas unidades de qué tipo
 * de producto se necesitan para satisfacer esta ranura de la oferta.
 *
 * <p>El campo {@code modoMatch} determina cómo se interpreta {@code idReferencia}:
 * <ul>
 *   <li>{@code COMIDA_ESPECIFICA} → idComida exacto.</li>
 *   <li>{@code COMIDA_POR_TIPO} → {@code tipoComidaFiltro} (FIJA/ESPECIAL); idReferencia=null.</li>
 *   <li>{@code PRODUCTO_ESPECIFICO} → idProductoCocina exacto.</li>
 *   <li>{@code PRODUCTO_POR_CATEGORIA} → idCategoria.</li>
 *   <li>{@code PRODUCTO_POR_SUBCATEGORIA} → idSubcategoria.</li>
 *   <li>{@code PRODUCTO_GENERAL} → cualquier ProductoCocina; idReferencia=null.</li>
 *   <li>{@code BASICO_ESPECIFICO} → idBasico exacto.</li>
 *   <li>{@code BASICO_GENERAL} → cualquier Basico; idReferencia=null.</li>
 *   <li>{@code COMBO_ESPECIFICO} → idCombo exacto.</li>
 *   <li>{@code COMBO_GENERAL} → cualquier Combo; idReferencia=null.</li>
 * </ul>
 * </p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "oferta_linea")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfertaLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta_linea")
    private Integer idOfertaLinea;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta")
    private Oferta oferta;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_match")
    private ModoMatchOferta modoMatch;

    /** Referencia al catálogo según {@code modoMatch}; null para modos _GENERAL y COMIDA_POR_TIPO. */
    @Column(name = "id_referencia")
    private Integer idReferencia;

    /** Solo para {@code COMIDA_POR_TIPO}: FIJA o ESPECIAL. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comida_filtro")
    private TipoComida tipoComidaFiltro;

    @Column(name = "cantidad")
    private int cantidad;
}
