package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Registro de consumo de insumos para la preparación de un platillo.
 *
 * <p>Permite llevar un conteo de cuánto se consumió de cada platillo, ya sea
 * en unidades ({@code cantidad}, ej: tortillas, piezas de pollo) o en peso
 * ({@code kilogramos}, ej: gramos de puré, pechuga). <strong>Al menos uno de
 * los dos campos debe tener valor</strong> — esto está garantizado por el
 * CHECK constraint {@code chk_inventario_no_vacio} en la base de datos.</p>
 *
 * <p>No puede eliminarse la {@link Comida} referenciada mientras tenga registros
 * de inventario (ON DELETE RESTRICT).</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link Comida}.</p>
 */
@Entity
@Table(name = "inventario_comida")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioComida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario_comida")
    private Integer idInventarioComida;

    /** Platillo al que corresponde este registro de consumo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida", nullable = false)
    private Comida comida;

    /**
     * Unidades consumidas (tortillas, piezas, etc.).
     * Al menos este campo o {@code kilogramos} debe tener valor (CHECK en DB).
     */
    @Column(name = "cantidad")
    private Integer cantidad;

    /**
     * Peso consumido en kilogramos (puré, pechuga, etc.).
     * Al menos este campo o {@code cantidad} debe tener valor (CHECK en DB).
     */
    @Column(name = "kilogramos", precision = 8, scale = 3)
    private BigDecimal kilogramos;
}
