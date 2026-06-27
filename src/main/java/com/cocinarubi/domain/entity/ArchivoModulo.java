package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo_modulo")
    private Integer idArchivoModulo;

    @Column(name = "nombre_modulo", nullable = false, length = 50)
    private String nombreModulo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_catalogo_producto")
    private TipoCatalogoProducto tipoCatalogoProducto;

    @Column(name = "ruta", nullable = false, length = 100)
    private String ruta;
    //jpeg,webp,json...
    @Column(name = "archivos_aceptados", nullable = false, columnDefinition = "JSON")
    private String archivosAceptados;
}
