package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.Estatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Platillo del menú de desayunos de Cocina rubi.
 *
 * <p>Los desayunos se sirven exclusivamente en modalidad <strong>PICK_UP</strong>
 * (recoger en tienda), de lunes a sábado de 7:00 a 11:00 h. Al igual que
 * {@link Comida}, están disponibles en porción media y entera.</p>
 *
 * <p>El {@code uuid_desayuno} es el identificador público para el menú web.</p>
 *
 * <p>Relaciones salientes: ninguna. Es referenciada por {@link DesayunoPedido}.</p>
 */
@Entity
@Table(name = "desayuno")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Desayuno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_desayuno")
    private Integer idDesayuno;

    @Column(name = "uuid_desayuno", nullable = false, length = 45)
    private String uuidDesayuno;

    @Column(name = "nombre_desayuno", nullable = false, length = 255)
    private String nombreDesayuno;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio_media", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMedia;

    @Column(name = "precio_entera", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioEntera;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false)
    private Estatus estatus;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;
}
