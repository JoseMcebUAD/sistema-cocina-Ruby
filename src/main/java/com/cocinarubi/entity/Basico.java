package com.cocinarubi.entity;

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

    /** Comida principal incluida en el paquete. No se puede eliminar si hay básicos activos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comida", nullable = false)
    private Comida comida;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** 1 = mostrar con prioridad visual en el menú web. */
    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    /** Precio total del paquete en pesos. Puede diferir de la suma individual de sus componentes. */
    @Column(name = "precio_basico", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioBasico;

    /** Complementos que conforman el paquete. Se eliminan en cascada si se borra el básico. */
    @Builder.Default
    @OneToMany(mappedBy = "basico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BasicoComplemento> complementos = new ArrayList<>();

    /** Sincroniza ambos lados de la relación al agregar un complemento al paquete. */
    public void addComplemento(BasicoComplemento basicoComplemento) {
        basicoComplemento.setBasico(this);
        this.complementos.add(basicoComplemento);
    }
}
