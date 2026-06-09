package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tarifa adicional activable manualmente para eventos especiales.
 *
 * <p>Ejemplo de uso: "Tarifa lluvia +$10". El operador activa la tarifa desde
 * el dashboard y el sistema la suma al costo de envío de los pedidos a domicilio
 * mientras {@code isActive} sea {@code true}.</p>
 *
 * <p>Si {@code ruta} es {@code null}, la tarifa aplica a todas las zonas de reparto.
 * Si apunta a una {@link Ruta} específica, solo afecta a los pedidos de esa zona.</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link Ruta} (nullable).</p>
 */
@Entity
@Table(name = "tarifa_especial")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa_lluvia")
    private Integer idTarifaLluvia;

    @Column(name = "nombre_tarifa", nullable = false, length = 255)
    private String nombreTarifa;

    /** Monto adicional que se suma al envío cuando esta tarifa está activa, en pesos. */
    @Column(name = "tarifa", nullable = false, precision = 5, scale = 2)
    private BigDecimal tarifa;

    /**
     * Ruta a la que aplica esta tarifa. Si es {@code null}, aplica a todas las rutas.
     * ON DELETE SET NULL: si se elimina la ruta, la tarifa pasa a ser global.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta")
    private Ruta ruta;

    /** Indica si la tarifa está actualmente activa. El operador la activa/desactiva manualmente. */
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
