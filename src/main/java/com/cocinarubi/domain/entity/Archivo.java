package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de un archivo subido a Cloudinary (imagen de producto, banner, etc.).
 *
 * <p>Cada archivo pertenece a un {@link ArchivoModulo} que define el contexto del
 * recurso. El campo {@code path_archivo} almacena la URL segura devuelta por
 * Cloudinary y {@code public_id} guarda el identificador único del recurso,
 * necesario para eliminar o transformar el archivo vía API.</p>
 *
 * <p>El campo {@code entity_type} clasifica el archivo dentro del catálogo de
 * productos. El campo {@code orden} permite controlar la posición del archivo
 * en galerías (por ejemplo, varias imágenes de la misma comida).</p>
 *
 * <p>El módulo no puede eliminarse si tiene archivos asociados (ON DELETE RESTRICT).</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link ArchivoModulo}.</p>
 */
@Entity
@Table(name = "archivo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo")
    private Integer idArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_archivo_modulo", nullable = false)
    private ArchivoModulo archivoModulo;

    @Column(name = "path_archivo", nullable = false, length = 255)
    private String pathArchivo;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "orden")
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private TipoCatalogoProducto entityType;

    @Column(name = "id_entidad", nullable = false)
    private Integer idEntidad;

    @Column(name = "public_id", nullable = false, unique = true, length = 255)
    private String publicId;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
