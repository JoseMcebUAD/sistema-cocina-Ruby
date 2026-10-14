package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de una aplicación concreta de una {@link Oferta} dentro de un {@link Pedido}.
 *
 * <p>Una misma oferta puede aparecer varias veces en el mismo pedido si el carrito
 * contiene suficientes productos coincidentes (ej. CA5: 2 comidas FIJA + 2 horchatas
 * genera 2 filas de {@code OfertaPedido}).</p>
 *
 * <p>El campo {@code precioAplicado} es un snapshot de {@link Oferta#getPrecioOferta()}
 * al momento de guardar; el campo {@code ahorro} registra cuánto se descontó respecto
 * a los precios normales de las líneas consumidas, para transparencia al cliente.</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "oferta_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfertaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta_pedido")
    private Integer idOfertaPedido;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    /** Puede ser null si la oferta fue eliminada del catálogo (ON DELETE RESTRICT evita esto en condiciones normales). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta")
    private Oferta oferta;

    @Column(name = "precio_aplicado")
    private BigDecimal precioAplicado;

    @Column(name = "ahorro")
    private BigDecimal ahorro;

    @Builder.Default
    @OneToMany(mappedBy = "ofertaPedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OfertaPedidoLinea> lineas = new ArrayList<>();
}
