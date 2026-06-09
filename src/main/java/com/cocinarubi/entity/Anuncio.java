package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Aviso publicado en el dashboard y en el menú web del cliente.
 *
 * <p>Se usa para notificar días festivos, cambios de horario, platillos
 * especiales o cualquier comunicado temporal. El campo {@code color} permite
 * destacar visualmente el anuncio según su importancia (valor HEX, ej. #FF5733).</p>
 *
 * <p>Relaciones: ninguna FK saliente ni entrante. Los anuncios son independientes
 * de cualquier entidad de negocio.</p>
 */
@Entity
@Table(name = "anuncio")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anuncio")
    private Integer idAnuncio;

    @Column(name = "descripcion_anuncio", nullable = false, length = 255)
    private String descripcionAnuncio;

    /** Color HEX del anuncio para resaltarlo en la interfaz. Ej: #FF5733. Puede ser nulo. */
    @Column(name = "color", length = 10)
    private String color;

    /** Fecha y hora de publicación del anuncio. */
    @Column(name = "fecha_anuncio", nullable = false)
    private LocalDateTime fechaAnuncio;
}
