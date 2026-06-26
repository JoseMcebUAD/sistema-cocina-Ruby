package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de un archivo subido al servidor (imagen de producto, banner, etc.).
 *
 * <p>Cada archivo pertenece a un {@link ArchivoModulo} que define la carpeta
 * destino y los tipos MIME permitidos. El campo {@code path_archivo} almacena
 * la ruta absoluta en el servidor para construir la URL pública del recurso.</p>
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

    @Column(name = "extension_archivo", nullable = false, length = 50)
    private String extensionArchivo;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
