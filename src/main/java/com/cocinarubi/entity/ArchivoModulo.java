package com.cocinarubi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Módulo de subida de archivos del sistema.
 *
 * <p>Define los contextos en los que se pueden cargar imágenes u otros archivos:
 * catálogo de comidas, desayunos, básicos, snacks, charolas y bebidas. Cada módulo
 * especifica la carpeta destino en el servidor ({@code ruta}) y los tipos MIME
 * permitidos ({@code archivos_aceptados}, almacenado como JSON array en MySQL).</p>
 *
 * <p>El campo {@code tipo_catalogo_producto} es {@code null} cuando el módulo no
 * corresponde a un producto del catálogo (ej: banners, anuncios).</p>
 *
 * <p>Relaciones salientes: ninguna. Referenciado por {@link Archivo}.</p>
 */
@Entity
@Table(name = "archivo_modulo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoModulo {

    /** Catálogo de producto al que puede asociarse un archivo subido en este módulo. */
    public enum TipoCatalogoProducto { BASICO, COMIDA, DESAYUNO, SNACK, CHAROLA, BEBIDA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo_modulo")
    private Integer idArchivoModulo;

    @Column(name = "nombre_modulo", nullable = false, length = 50)
    private String nombreModulo;

    /**
     * Tipo de catálogo de producto al que pertenece este módulo.
     * {@code null} si el módulo no está asociado a un producto (ej: banners).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_catalogo_producto")
    private TipoCatalogoProducto tipoCatalogoProducto;

    /** Carpeta destino en el servidor de archivos (no la ruta de zona de reparto). */
    @Column(name = "ruta", nullable = false, length = 100)
    private String ruta;

    /**
     * Array JSON de MIME types permitidos. Ej: ["image/jpeg","image/png"].
     * Se almacena como JSON en MySQL; se lee como String y se parsea en la capa de servicio.
     */
    @Column(name = "archivos_aceptados", nullable = false, columnDefinition = "JSON")
    private String archivosAceptados;
}
