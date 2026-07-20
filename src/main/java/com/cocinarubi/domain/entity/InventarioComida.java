package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoContadorComida;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida", nullable = false)
    private Comida comida;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "tipo_contador_comida", precision = 8, scale = 3)
    private TipoContadorComida tipo_contador_comida;
}
