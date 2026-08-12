package com.cocinarubi.domain.service.files;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cocinarubi.dao.ArchivoModuloRepository;
import com.cocinarubi.domain.entity.ArchivoModulo;
import com.cocinarubi.domain.entity.Categoria;
import com.cocinarubi.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona el ciclo de vida de las filas de {@code archivo_modulo} asociadas a
 * una {@link Categoria} dinámica. Cada categoría creada por el usuario genera
 * automáticamente un módulo con carpeta {@code cocina_rubi/<nombre-lower>} y
 * los MIME types de imagen estándar.
 *
 * <p>Cloudinary crea las carpetas al primer upload, pero invocamos
 * {@code createFolder} para que la UI del usuario vea la carpeta antes de subir.
 * La llamada es best-effort: se registra un warning si falla.</p>
 *
 * <p>Capa: Service — infraestructura del subsistema de archivos.</p>
 */
@Service
public class ArchivoModuloService {

    private static final Logger log = LoggerFactory.getLogger(ArchivoModuloService.class);

    // MIME types por defecto (mismos que en el seeder V14 para tipos estáticos)
    private static final String MIMES_POR_DEFECTO =
            "[\"image/jpeg\",\"image/png\",\"image/webp\"]";

    private final ArchivoModuloRepository archivoModuloRepository;
    private final ArchivoModuloCache archivoModuloCache;
    private final Cloudinary cloudinary;

    public ArchivoModuloService(ArchivoModuloRepository archivoModuloRepository,
                                ArchivoModuloCache archivoModuloCache,
                                Cloudinary cloudinary) {
        this.archivoModuloRepository = archivoModuloRepository;
        this.archivoModuloCache = archivoModuloCache;
        this.cloudinary = cloudinary;
    }

    /**
     * Crea la fila {@code archivo_modulo} correspondiente a una nueva categoría
     * y refresca el caché. Best-effort: intenta crear la carpeta en Cloudinary
     * pero no aborta si falla (Cloudinary la creará al primer upload).
     */
    @Transactional
    public ArchivoModulo crearParaCategoria(Categoria categoria) {
        String ruta = "cocina_rubi/" + categoria.getNombre().toLowerCase();

        ArchivoModulo modulo = ArchivoModulo.builder()
                .nombreModulo(categoria.getNombre())
                .tipoCatalogoProducto(null)
                .categoria(categoria)
                .ruta(ruta)
                .archivosAceptados(MIMES_POR_DEFECTO)
                .build();

        ArchivoModulo persistido = archivoModuloRepository.save(modulo);
        archivoModuloCache.refresh();

        crearCarpetaCloudinary(ruta);

        return persistido;
    }

    /**
     * Actualiza la ruta de un módulo cuando la categoría cambia de nombre.
     * Los archivos ya subidos conservan su ruta original hasta ser resubidos.
     */
    @Transactional
    public void actualizarRutaParaCategoria(Categoria categoria) {
        ArchivoModulo modulo = archivoModuloRepository
                .findByCategoria_IdCategoria(categoria.getIdCategoria())
                .orElseThrow(() -> new BusinessException(
                        "No existe módulo para la categoría id: " + categoria.getIdCategoria(),
                        HttpStatus.NOT_FOUND));

        String nuevaRuta = "cocina_rubi/" + categoria.getNombre().toLowerCase();
        if (nuevaRuta.equals(modulo.getRuta())) {
            return;
        }
        modulo.setNombreModulo(categoria.getNombre());
        modulo.setRuta(nuevaRuta);
        archivoModuloRepository.save(modulo);
        archivoModuloCache.refresh();

        crearCarpetaCloudinary(nuevaRuta);
    }

    /**
     * Elimina el módulo asociado a una categoría. Los archivos hijos se
     * eliminan por CASCADE (ver migración V25). Idempotente: no falla si el
     * módulo no existe.
     */
    @Transactional
    public void eliminarParaCategoria(int idCategoria) {
        archivoModuloRepository.findByCategoria_IdCategoria(idCategoria)
                .ifPresent(modulo -> {
                    archivoModuloRepository.delete(modulo);
                    archivoModuloCache.refresh();
                });
    }

    private void crearCarpetaCloudinary(String ruta) {
        try {
            // La SDK expone create_folder en api(); si falla, Cloudinary igual
            // creará la carpeta al primer upload de un archivo con ese folder.
            cloudinary.api().createFolder(ruta, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("No se pudo crear la carpeta en Cloudinary '{}': {}", ruta, e.getMessage());
        }
    }
}
