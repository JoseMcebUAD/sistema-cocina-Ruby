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
 * <p>Discriminador dual: cada fila usa <b>exactamente uno</b> de
 * {@code entity_type} (BASICO/COMIDA/DESAYUNO) o {@code id_categoria}
 * (categorías dinámicas). CHECK constraint en BD.</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link ArchivoModulo} y a
 * {@link Categoria} (nullable).</p>
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
    @JoinColumn(name = "id_archivo_modulo")
    private ArchivoModulo archivoModulo;

    @Column(name = "path_archivo")
    private String pathArchivo;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "orden")
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private TipoCatalogoProducto entityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(name = "id_entidad")
    private Integer idEntidad;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "creadoEn")
    private LocalDateTime creadoEn;
}
