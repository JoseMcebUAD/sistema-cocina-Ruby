package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Tabla pivote que asocia un {@link Complemento} a un paquete {@link Basico}.
 *
 * <p>Define qué complementos están incluidos en un paquete básico sin costo adicional
 * (a menos que {@link Complemento#isCobrarSiempre()} sea {@code true}). Esta relación
 * se carga junto con el básico para precargar el carrito del cliente (RF07).</p>
 *
 * <p>Se elimina en cascada ({@code ON DELETE CASCADE}) si se borra el básico padre.
 * El complemento referenciado no se puede eliminar mientras existan básicos que lo usen
 * ({@code ON DELETE RESTRICT}).</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link Basico} — paquete al que pertenece.</li>
 *   <li>{@code @ManyToOne} LAZY a {@link Complemento} — acompañamiento incluido.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "basico_complemento")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicoComplemento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_basico_complemento")
    private Integer idBasicoComplemento;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_basico", nullable = false)
    private Basico basico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_complemento", nullable = false)
    private Complemento complemento;
}
