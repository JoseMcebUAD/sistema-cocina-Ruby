package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoPedido;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Regla de precio especial que se aplica automáticamente cuando un {@link Pedido}
 * contiene la combinación de productos definida en sus {@link OfertaLinea}s.
 *
 * <p>A diferencia de {@link Combo} (el cliente lo elige explícitamente), una
 * Oferta es detectada y aplicada por el backend en el momento de guardar el pedido.
 * Cada coincidencia genera un {@link OfertaPedido} con su precio fijo.</p>
 *
 * <p>El campo {@code prioridad} controla el orden greedy del algoritmo de matching:
 * mayor prioridad se intenta primero. Ofertas con igual prioridad se aplican
 * en el orden devuelto por la consulta (id ASC).</p>
 *
 * <p>Capa: Entity — persistencia.</p>
 */
@Entity
@Table(name = "oferta")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Integer idOferta;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio_oferta")
    private BigDecimal precioOferta;

    @Column(name = "prioridad")
    private int prioridad;

    @Column(name = "activo")
    private boolean activo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Builder.Default
    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OfertaLinea> lineas = new ArrayList<>();

    /**
     * Tipos de pedido en los que aplica esta oferta.
     * Set vacío = sin restricción (aplica a cualquier tipo).
     * Con valores = solo aplica cuando {@code pedido.tipoPedido} esté en el set.
     */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oferta_tipo_pedido", joinColumns = @JoinColumn(name = "id_oferta"))
    @Column(name = "tipo_pedido")
    @Enumerated(EnumType.STRING)
    private Set<TipoPedido> tiposPedidoRestriccion = new HashSet<>();

    public void addLinea(OfertaLinea linea) {
        linea.setOferta(this);
        this.lineas.add(linea);
    }

    /** @return {@code true} si la oferta aplica al tipo dado; set vacío = sin restricción. */
    public boolean estaDisponiblePara(TipoPedido tipo) {
        return tiposPedidoRestriccion == null || tiposPedidoRestriccion.isEmpty()
                || tiposPedidoRestriccion.contains(tipo);
    }

    /**
     * @return {@code true} si {@code hoy} cae dentro del rango de vigencia.
     * Fecha nula = sin restricción por ese extremo (null fechaInicio = desde siempre; null fechaFin = hasta siempre).
     */
    public boolean estaVigente(LocalDate hoy) {
        if (fechaInicio != null && hoy.isBefore(fechaInicio)) return false;
        if (fechaFin    != null && hoy.isAfter(fechaFin))     return false;
        return true;
    }
}
