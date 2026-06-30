package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Paquete predefinido que agrupa una comida con sus complementos a un precio fijo (RF07).
 *
 * <p>El operador crea un básico eligiendo una {@link Comida} y los {@link Complemento}s
 * que lo acompañan. Cuando el cliente selecciona un básico en el menú web, el sistema
 * precarga automáticamente sus complementos sin que el cliente tenga que elegirlos
 * uno a uno. El precio del paquete puede ser menor que la suma individual.</p>
 *
 * <p>Relaciones:
 * <ul>
 *   <li>{@code @ManyToOne} LAZY a {@link Comida} — platillo base del paquete.</li>
 *   <li>{@code @OneToMany} LAZY a {@link BasicoComplemento} — complementos incluidos.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "basico")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Basico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_basico")
    private Integer idBasico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida", nullable = false)
    private Comida comida;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    @Column(name = "precio_basico", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioBasico;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private Estatus estatus = Estatus.DISPONIBLE;

    @Builder.Default
    @OneToMany(mappedBy = "basico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BasicoComplemento> complementos = new ArrayList<>();

    public void addComplemento(BasicoComplemento basicoComplemento) {
        basicoComplemento.setBasico(this);
        this.complementos.add(basicoComplemento);
    }
}
