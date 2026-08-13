package com.cocinarubi.domain.entity;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Módulo de subida de archivos del sistema.
 *
 * <p>Define los contextos en los que se pueden cargar imágenes u otros archivos.
 * Cada módulo especifica la carpeta destino en Cloudinary ({@code ruta}) y los
 * tipos MIME permitidos ({@code archivos_aceptados}, JSON array).</p>
 *
 * <p>Discriminador dual: cada fila se identifica por <b>exactamente uno</b> de
 * los siguientes campos (CHECK constraint en BD):
 * <ul>
 *   <li>{@code tipo_catalogo_producto} para los tres módulos estáticos
 *       (BASICO, COMIDA, DESAYUNO).</li>
 *   <li>{@code id_categoria} para los módulos dinámicos generados al crear
 *       una {@link Categoria}.</li>
 * </ul></p>
 *
 * <p>Relaciones salientes: {@code @ManyToOne} LAZY a {@link Categoria}
 * (nullable). Referenciado por {@link Archivo}.</p>
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

    @Column(name = "nombre_modulo")
    private String nombreModulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_catalogo_producto")
    private TipoCatalogoProducto tipoCatalogoProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(name = "ruta")
    private String ruta;

    //jpeg,webp,json...
    @Column(name = "archivos_aceptados")
    private String archivosAceptados;
}
