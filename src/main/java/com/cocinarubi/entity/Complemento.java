package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Acompañamiento opcional o incluido que se puede agregar a una comida.
 *
 * <p>Un complemento puede ser gratuito ({@code precio_extra = 0}) o tener costo
 * adicional. El flag {@code cobrarSiempre} indica que debe cobrarse incluso cuando
 * la comida forma parte de un paquete {@link Basico}; sin él, los complementos
 * incluidos en el básico no generan cargo extra.</p>
 *
 * <p>El {@code uuid_complemento} es el identificador público para el menú web.</p>
 *
 * <p>Relaciones salientes: ninguna. Referenciado por {@link BasicoComplemento}
 * y {@link ComplementoComidaPedido}.</p>
 */
@Entity
@Table(name = "complemento")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complemento {

    public enum Estatus { DISPONIBLE, NO_DISPONIBLE, AGOTADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_complemento")
    private Integer idComplemento;

    @Column(name = "uuid_complemento", nullable = false, length = 45)
    private String uuidComplemento;

    @Column(name = "nombre_complemento", nullable = false, length = 255)
    private String nombreComplemento;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio_extra", nullable = false, precision = 5, scale = 2)
    private BigDecimal precioExtra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private Estatus estatus;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    @Column(name = "cobrar_siempre", nullable = false)
    private boolean cobrarSiempre;
}
