package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.PrioridadAnuncio;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aviso publicado en el dashboard y en el menú web del cliente.
 *
 * <p>Soporta prioridad (ALTA/MEDIA/BAJA), estado activo/inactivo, una imagen banner
 * alojada en Cloudinary y asociaciones polimórficas opcionales a productos del catálogo
 * para destacarlos en el carrito del cliente.</p>
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

    @Column(name = "descripcion_anuncio")
    private String descripcionAnuncio;

    @Column(name = "color")
    private String color;

    @Column(name = "fecha_expiracion_anuncio")
    private LocalDateTime fechaExpiracionAnuncio;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad")
    private PrioridadAnuncio prioridad;

    @Builder.Default
    @Column(name = "activo")
    private boolean activo = true;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "imagen_public_id")
    private String imagenPublicId;

    @Builder.Default
    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnuncioProducto> productos = new ArrayList<>();
}
